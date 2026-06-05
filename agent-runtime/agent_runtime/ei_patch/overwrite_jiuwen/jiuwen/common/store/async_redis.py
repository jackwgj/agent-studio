#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""the redis utils for the action about redis"""

import os
import ssl
import time
from dataclasses import dataclass
from enum import Enum
from functools import wraps
from typing import Dict, List, Optional, Union

import aiohttp
import redis.asyncio as redis
from jiuwen.common.alarm.hwclouds_alarm_utils import (
    record_alarm,
)  # @override(jiuwen) added alarm logging
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import interface_logger, logger
from jiuwen.common.net import Connector
from jiuwen.common.security.cryptor import Crypt
from jiuwen.common.utils.singleton import Singleton
from redis.asyncio.cluster import ClusterNode, RedisCluster

DEFAULT_REDIS_TTL = 259200
DEFAULT_REDIS_DB = 0
DEFAULT_REDIS_MAX_CONNECTIONS = 10
DEFAULT_REDIS_SSL_MODE = "true"
DEFAULT_SOCKET_TIMEOUT = 5
DEFAULT_SOCKET_CONNECT_TIMEOUT = 5
DEFAULT_REDIS_HOST = "localhost"
DEFAULT_REDIS_PORT = 6379
REDIS_CHECK_LOCK_KEY = "runtime:redis_check"
REDIS_CHECK_TIME_DIFF = 5
REDIS_CHECK_TIME_MIN = 30


def interface_log(path):
    def decorator(func):
        async def wrapper(*args, **kwargs):
            start_time = time.perf_counter()

            ret_code, ret_data = await func(*args, **kwargs)

            end_time = time.perf_counter()
            duration = round((end_time - start_time) * 1000)
            interface_logger.info(
                f"redis_detect|{path}|{ret_data}|{ret_code}|{duration}"
            )
            return ret_code

        return wrapper

    return decorator


class RedisDetection(metaclass=Singleton):
    _REPORT_STATUS_URL = "/gateway/eventReport"

    _TIMEOUT = 60

    def __init__(self):
        self.base_url = os.getenv("REDIS_DETECTION_BASE_URL")

    @interface_log(path=_REPORT_STATUS_URL)
    async def report_fail(self):
        try:
            fail_report_data = {"tranCode": "REDIS_FAILED", "eventCode": "REDIS_FAILED"}
            headers = {"Content-Type": "application/json"}
            connector = Connector().get_tcp_connector()
            async with aiohttp.ClientSession(
                connector=connector, connector_owner=connector is None
            ) as session:
                async with session.post(
                    url=f"{self.base_url}{RedisDetection._REPORT_STATUS_URL}",
                    json=fail_report_data,
                    headers=headers,
                    ssl=False,
                    timeout=aiohttp.ClientTimeout(
                        total=RedisDetection._TIMEOUT,
                        connect=RedisDetection._TIMEOUT,
                        sock_connect=RedisDetection._TIMEOUT,
                    ),
                ) as res:
                    await res.read()
                    return res.status, "REDIS_FAILED"
        except Exception as e:
            # redis上报与主流程无关，不中断主流程
            logger.error(f"post redis detection fail, {e}")
            return -1, ""


def report_redis_fail(func):
    @wraps(func)
    async def wrapper(*args, **kwargs):
        try:
            return await func(*args, **kwargs)
        except Exception as e:
            record_alarm(
                "redis", "redis", "ERROR", str(e)
            )  # @override(jiuwen) added alarm logging
            if os.getenv("REDIS_DETECTION_BASE_URL"):
                await RedisDetection().report_fail()
            raise e  # 重新抛出异常，不吞掉

    return wrapper


class RedisMode(Enum):
    CLUSTER: str = "cluster"
    SINGLE: str = "single"


@dataclass
class RedisConfig:
    host: str
    port: str
    passwd: str
    ssl_mode: str
    ssl_passwd: str
    cluster_node: str
    ssl_ca_certs: str
    ssl_certfile: str
    ssl_keyfile: str
    ttl: int
    db: int
    max_connections: int
    mode: str
    socket_timeout: int
    socket_connect_timeout: int


class AsyncRedisUtils:
    """
    Redis operation tool class.
    """

    # Class variable used to store connection pool instances
    _connection_pool = None
    _cluster = None

    def __init__(self, config: RedisConfig):
        try:
            # During initialization, the system checks whether the connection pool exists. If the connection pool
            # does not exist, the system creates the connection pool.
            ssl_mode = config.ssl_mode
            if config.mode == RedisMode.CLUSTER.value:
                # 集群配置
                if AsyncRedisUtils._cluster is None:
                    if str(ssl_mode).lower() == "false":
                        # 关闭ssl认证
                        AsyncRedisUtils._cluster = self._get_redis_cluster_without_ssl(
                            config
                        )
                    else:
                        AsyncRedisUtils._cluster = self._get_redis_cluster_with_ssl(
                            config
                        )
                self._redis = AsyncRedisUtils._cluster
            else:
                # 单服务配置
                if AsyncRedisUtils._connection_pool is None:
                    if str(ssl_mode).lower() == "false":
                        # 关闭ssl认证
                        AsyncRedisUtils._connection_pool = (
                            self._get_connection_pool_without_ssl(config)
                        )
                    else:
                        AsyncRedisUtils._connection_pool = (
                            self._get_connection_pool_with_ssl(config)
                        )
                self._redis = redis.Redis(
                    connection_pool=AsyncRedisUtils._connection_pool
                )
            # 清理掉内存中的密码信息
            config.passwd = ""
            config.ssl_passwd = ""
        except redis.ConnectionError as e:
            record_alarm(
                "redis", "redis", "ERROR", f"failed to connect redis. {e}"
            )  # @override(jiuwen) added alarm logging
            logger.error(f"failed to async connect to the redis: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e
        except Exception as e:
            record_alarm(
                "redis", "redis", "ERROR", f"failed to connect redis. {e}"
            )  # @override(jiuwen) added alarm logging
            logger.error(f"An error occurred while initializing the async Redis.: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e

    async def init(self):
        await self.ping()

    @staticmethod
    def get_cluster_nodes():
        """
        获取集群节点信息
        """
        try:
            return (
                AsyncRedisUtils._cluster.get_nodes() if AsyncRedisUtils._cluster else []
            )
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            record_alarm(
                "redis", "redis", "ERROR", f"failed to get redis cluster nodes. {e}"
            )  # @override(jiuwen) added alarm logging
            raise JiuWenBaseException(
                StatusCode.REDIS_CLUSTER_NODES_GET_ERROR.code,
                StatusCode.REDIS_CLUSTER_NODES_GET_ERROR.errmsg,
            ) from e

    @staticmethod
    def _get_connection_pool_with_ssl(config: RedisConfig):
        """Obtaining the Secure Connection Pool"""
        if config is None:
            config = {}
        return redis.ConnectionPool(
            connection_class=redis.SSLConnection,
            host=config.host,
            port=config.port,
            db=config.db,
            password=config.passwd,
            ssl_cert_reqs=ssl.CERT_REQUIRED,
            ssl_ca_certs=config.ssl_ca_certs,
            ssl_certfile=config.ssl_certfile,
            ssl_keyfile=config.ssl_keyfile,
            ssl_password=config.ssl_passwd,
            max_connections=config.max_connections,
            socket_timeout=config.socket_timeout,
        )

    @staticmethod
    def _get_redis_cluster_with_ssl(config: RedisConfig):
        if config is None:
            config = {}
        return RedisCluster(
            startup_nodes=AsyncRedisUtils._get_startup_nodes_of_redis_cluster(config),
            password=config.passwd,
            ssl_cert_reqs=ssl.CERT_REQUIRED,
            ssl_ca_certs=config.ssl_ca_certs,
            ssl_certfile=config.ssl_certfile,
            ssl_keyfile=config.ssl_keyfile,
            ssl_password=config.ssl_passwd,
            decode_responses=False,
            socket_timeout=config.socket_timeout,
            socket_connect_timeout=config.socket_connect_timeout,
            max_connections=config.max_connections,
        )

    @staticmethod
    def _get_startup_nodes_of_redis_cluster(config: RedisConfig) -> List[ClusterNode]:
        """
        获取Redis集群节点host与port配置列表
        """
        startup_nodes_str = config.cluster_node
        startup_nodes_list = [
            startup_node.strip() for startup_node in startup_nodes_str.split(",")
        ]
        startup_nodes = []
        try:
            for startup_node in startup_nodes_list:
                host, port = startup_node.split(":")
                startup_nodes.append(ClusterNode(host=host, port=int(port)))
            return startup_nodes
        except Exception as e:
            record_alarm(
                "redis",
                "redis",
                "ERROR",
                f"get_startup_nodes_of_redis_cluster failed. {e}",
            )  # @override(jiuwen) added alarm logging
            raise JiuWenBaseException(
                error_code=StatusCode.REDIS_CLUSTER_NODES_CONFIGURATION_ERROR.code,
                message=StatusCode.REDIS_CLUSTER_NODES_CONFIGURATION_ERROR.errmsg,
            ) from e

    @staticmethod
    def _get_connection_pool_without_ssl(config: RedisConfig):
        """Get connection pool without ssl"""
        if config is None:
            config = {}
        return redis.ConnectionPool(
            host=config.host,
            port=config.port,
            db=config.db,
            password=config.passwd,
            max_connections=config.max_connections,
            socket_timeout=config.socket_timeout,
        )

    @staticmethod
    def _get_redis_cluster_without_ssl(config: RedisConfig):
        if config is None:
            config = {}
        return RedisCluster(
            startup_nodes=AsyncRedisUtils._get_startup_nodes_of_redis_cluster(config),
            password=config.passwd,
            decode_responses=False,
            socket_timeout=config.socket_timeout,
            socket_connect_timeout=config.socket_connect_timeout,
            max_connections=config.max_connections,
        )

    @report_redis_fail
    async def list_append(self, key, *values, ttl):
        """
        向列表尾部添加一个或多个值
        """
        try:
            await self._redis.expire(key, ttl)
            return await self._redis.rpush(key, *values)
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_insert(self, key, index, value):
        """
        在列表的指定位置插入值
        """
        try:
            return await self._redis.linsert(key, "AFTER", index, value)
        except Exception as e:
            logger.error(f"error inserting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_remove(self, key, count, value):
        """
        从列表中移除元素
        """
        try:
            return await self._redis.lrem(key, count, value)
        except Exception as e:
            logger.error(f"Error removing element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_REMOVE_ELEMENTS_FAILED.code,
                StatusCode.REDIS_REMOVE_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_set(self, key, index, value):
        """
        设置列表在指定位置的值
        """
        try:
            await self._redis.lset(key, index, value)
            return True
        except redis.RedisError as e:
            logger.error(f"Error setting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SET_SPECIFIED_ELEMENTS_FAILED.code,
                StatusCode.REDIS_SET_SPECIFIED_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_get(self, key, start=0, end=-1):
        """
        获取列表中一段元素
        """
        try:
            return await self._redis.lrange(key, start, end)
        except Exception as e:
            logger.error(f"Error getting list: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_contain(self, key):
        """
        确认是否存在某个key
        """
        try:
            return await self._redis.exists(key)
        except Exception as e:
            logger.error(f"Error getting list: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_pop(self, key):
        """
        从列表头部弹出元素
        """
        try:
            return await self._redis.lpop(key)
        except Exception as e:
            logger.error(f"Error ejecting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_POP_ELEMENTS_FAILED.code,
                StatusCode.REDIS_POP_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_tail_pop(self, key):
        """
        从列表尾部弹出元素, 栈
        """
        try:
            return await self._redis.rpop(key)
        except Exception as e:
            logger.error(f"Error ejecting tail element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_POP_ELEMENTS_FAILED.code,
                StatusCode.REDIS_POP_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def list_length(self, key):
        """
        获取列表长度
        """
        try:
            return await self._redis.llen(key)
        except Exception as e:
            logger(f"获取列表长度时出错: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LENGTH_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LENGTH_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def get(self, key):
        """
        根据k获取Redis中的元素
        """
        try:
            return await self._redis.get(key)
        except Exception as e:
            logger.error(f"Error getting element from redis, the key is {key}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def set(
        self,
        key,
        value,
        ex=int(os.getenv("DATASOURCE_REDIS_TTL", DEFAULT_REDIS_TTL)),
        nx=False,
    ):
        """
        向Redis中的添加元素
        """
        try:
            return await self._redis.set(key, value, ex, None, nx)
        except Exception as e:
            logger.error(f"Error setting element to redis, the key is {key}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SET_ELEMENT_FAILED.code,
                StatusCode.REDIS_SET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def delete(self, key):
        """
        从Redis中的删除元素
        """
        try:
            return await self._redis.delete(key)
        except Exception as e:
            logger.error(f"Error deleting element from redis, the key is {key}")
            raise JiuWenBaseException(
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.code,
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def ping(self):
        """
        测试Redis是否正常连接
        """
        try:
            return await self._redis.ping()
        except Exception as e:
            logger.error("Error ping redis")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e

    @report_redis_fail
    async def hash_set(self, name, key=None, value=None, mapping=None, ex: int = None):
        """
        redis hset() 方法的定时过期封装。
        """
        try:
            ex = ex or int(os.getenv("DATASOURCE_REDIS_TTL", DEFAULT_REDIS_TTL))
            num = await self._redis.hset(name, key, value, mapping)
            await self._redis.expire(name, ex)
            return num
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def hash_get(self, name, key):
        """
        redis hget() 方法的封装。
        """
        try:
            return await self._redis.hget(name, key)
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def hash_getall(self, name):
        """
        redis hgetall() 方法的封装。
        """
        try:
            return await self._redis.hgetall(name)
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def get_redis_client(self):
        """
        返回redis client实例
        """
        return self._redis

    @report_redis_fail
    async def ltrim(self, name: str, start: int, end: int = -1):
        """
        从Redis中的删除元素
        """
        try:
            return await self._redis.ltrim(name, start, end)
        except Exception as e:
            logger.error("Error ltrim redis")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e

    @report_redis_fail
    async def expire(self, key: str, ttl: int):
        """
        从Redis中刷新时间
        """
        try:
            await self._redis.expire(key, ttl)
        except Exception as e:
            logger.error(f"Error expire element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def get_by_prefix(self, prefix: str) -> Dict[str, Union[str]]:
        """
        根据key的前缀批量获取value值
        """
        try:
            client = self._redis
            result = {}
            pattern = f"{prefix}*"

            # 使用SCAN进行游标式查询，避免阻塞
            if hasattr(client, "scan_iter"):
                # redis-py 2.6+ 使用 scan_iter
                async for key in client.scan_iter(match=pattern):
                    value = await client.get(key)
                    if value is not None:
                        # 统一处理键和值的 bytes 类型
                        key_str = key.decode() if isinstance(key, bytes) else key
                        value_str = (
                            value.decode() if isinstance(value, bytes) else value
                        )
                        result[key_str] = value_str
            else:
                # 兼容集群模式
                cursor = b"0"
                while True:
                    cursor, keys = await client.scan(
                        cursor=cursor, match=pattern, count=100
                    )
                    for key in keys:
                        value = await client.get(key)
                        if value is not None:
                            # 统一处理键和值的 bytes 类型
                            key_str = key.decode() if isinstance(key, bytes) else key
                            value_str = (
                                value.decode() if isinstance(value, bytes) else value
                            )
                            result[key_str] = value_str
                    if cursor == b"0":
                        break

            return result
        except Exception as e:
            logger.error(f"Error get elements by prefix: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def delete_by_prefix(self, prefix: str) -> int:
        """
        根据key的前缀批量删除value值
        """
        try:
            client = self._redis
            deleted_count = 0
            pattern = f"{prefix}*"

            # 分批删除，避免一次性删除大量键带来的性能问题
            batch_size = 1000
            total_batch = 0

            while True:
                if hasattr(client, "scan_iter"):
                    # redis-py 2.6+ 使用 scan_iter
                    keys_batch = []
                    async for key in client.scan_iter(match=pattern):
                        keys_batch.append(key)
                        if len(keys_batch) >= batch_size:
                            break

                    if not keys_batch:
                        break

                    # 使用管道删除这一批键
                    pipe = client.pipeline()
                    for key in keys_batch:
                        pipe.delete(key)
                    pipe_result = await pipe.execute()
                    deleted_count += sum(batch if batch else 0 for batch in pipe_result)

                    # 如果这批获取的键少于批大小，说明已经遍历完毕
                    if len(keys_batch) < batch_size:
                        break
                else:
                    # 兼容集群模式
                    cursor = b"0"
                    while True:
                        cursor, keys_batch = await client.scan(
                            cursor=cursor, match=pattern, count=batch_size
                        )
                        if not keys_batch:
                            break

                        # 使用管道删除这批键
                        pipe = client.pipeline()
                        for key in keys_batch:
                            pipe.delete(key)
                        pipe_result = await pipe.execute()
                        deleted_count += sum(
                            batch if batch else 0 for batch in pipe_result
                        )

                        if cursor == b"0":
                            break

                total_batch += 1
                # 防无限循环的安全措施（最多100批）
                if total_batch > 100:
                    logger.warning(
                        f"delete by prefix over limit batch num（100）, stop it.There maybe some keys exists for: prefix={prefix}"
                    )
                    break

            return deleted_count
        except Exception as e:
            logger.error(f"Error delete elements by prefix: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.code,
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    async def mget(self, keys: List[str]) -> List[Optional[Union[str]]]:
        """
        批量获取多个键的值
        """
        try:
            client = self._redis
            values = await client.mget(keys)
            result = []
            for value in values:
                if value is None:
                    result.append(None)
                else:
                    result.append(value)
            return result
        except Exception as e:
            logger.error(f"Error getting element from redis, the keys is {keys}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e


def get_redis_config() -> RedisConfig:
    """
    Get redis config from the environment.
    """
    redis_config = RedisConfig(
        host=os.getenv("DATASOURCE_REDIS_HOST") or DEFAULT_REDIS_HOST,
        port=os.getenv("DATASOURCE_REDIS_PORT") or DEFAULT_REDIS_PORT,
        passwd=os.getenv("DATASOURCE_REDIS_PASSWORD"),
        ssl_mode=str(
            os.environ.get("DATASOURCE_REDIS_SSL_MODE", DEFAULT_REDIS_SSL_MODE)
        ).lower(),
        ssl_passwd=os.getenv("DATASOURCE_REDIS_SSL_PASSWD", ""),
        cluster_node=os.getenv("DATASOURCE_REDIS_CLUSTER_NODE", ""),
        ssl_ca_certs=os.environ.get("DATASOURCE_REDIS_SSL_CA_CERTS", ""),
        ssl_certfile=os.environ.get("DATASOURCE_REDIS_SSL_CERTFILE", ""),
        ssl_keyfile=os.environ.get("DATASOURCE_REDIS_SSL_KEYFILE", ""),
        ttl=int(os.getenv("DATASOURCE_REDIS_TTL") or DEFAULT_REDIS_TTL),
        db=int(os.getenv("DATASOURCE_REDIS_DATABASE") or DEFAULT_REDIS_DB),
        max_connections=int(
            os.getenv("DATASOURCE_REDIS_MAX_CONNECTIONS")
            or DEFAULT_REDIS_MAX_CONNECTIONS
        ),
        mode=os.getenv("DATASOURCE_REDIS_MODE") or RedisMode.SINGLE.value,
        socket_timeout=int(
            os.getenv("DATASOURCE_REDIS_SOCKET_TIMEOUT") or DEFAULT_SOCKET_TIMEOUT
        ),
        socket_connect_timeout=int(
            os.getenv("DATASOURCE_REDIS_SOCKET_CONNECT_TIMEOUT")
            or DEFAULT_SOCKET_CONNECT_TIMEOUT
        ),
    )
    return redis_config


def get_async_redis_instance(redis_config: Optional[RedisConfig] = None):
    """
    获取Redis实例
    根据配置
    """
    if redis_config is None:
        redis_config = get_redis_config()
    ssl_passwd = redis_config.ssl_passwd
    ssl_mode = redis_config.ssl_mode
    curr_work_api = Crypt()
    redis_config.passwd = curr_work_api.decrypt(redis_config.passwd)
    if ssl_mode != "false":
        # 当用户未关闭ssl加密认证的时候，ssl_passwd为必填项
        if ssl_passwd:
            redis_config.ssl_passwd = curr_work_api.decrypt(ssl_passwd)
        else:
            logger.error(
                "An error occurred while initializing the Redis: ssl_passwd is necessary when SSL is enabled."
            )
            raise JiuWenBaseException(
                error_code=StatusCode.REDIS_SERVICE_INIT_FAILED.code,
                message=StatusCode.REDIS_SERVICE_INIT_FAILED.errmsg,
            )

    return AsyncRedisUtils(redis_config)
