#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""the redis utils for the action about redis"""

import os
import sched
import ssl
import threading
import time
import uuid
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from enum import Enum
from functools import wraps
from typing import List, Optional, Dict, Union

import requests
from jiuwen.common.alarm.hwclouds_alarm_utils import (
    record_alarm,
)  # @override(jiuwen) added alarm logging
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger, interface_logger
from jiuwen.common.security.cryptor import Crypt
from jiuwen.common.utils.singleton import Singleton
from redis.cluster import RedisCluster, ClusterNode

import redis

DEFAULT_REDIS_TTL = 259200
DEFAULT_REDIS_DB = 0
DEFAULT_REDIS_MAX_CONNECTIONS = 10
DEFAULT_REDIS_SSL_MODE = "true"
DEFAULT_SOCKET_TIMEOUT = 5
DEFAULT_SOCKET_CONNECT_TIMEOUT = 5
DEFAULT_REDIS_PORT = 6379
DEFAULT_REDIS_HOST = "localhost"
REDIS_CHECK_LOCK_KEY = "runtime:redis_check"
REDIS_CHECK_TIME_DIFF = 5
REDIS_CHECK_TIME_MIN = 30

executor = ThreadPoolExecutor(max_workers=20)


def interface_log(path):
    def decorator(func):
        def wrapper(*args, **kwargs):
            start_time = time.perf_counter()

            ret_code, ret_data = func(*args, **kwargs)

            end_time = time.perf_counter()
            duration = round((end_time - start_time) * 1000)
            interface_logger.info(
                f"redis_detect|{path}|{ret_data}|{ret_code}|{duration}"
            )
            return ret_code

        return wrapper

    return decorator


class RedisDetection(metaclass=Singleton):
    _HEAT_BEAT_URL = "/gateway/usabilityReport"

    _REPORT_STATUS_URL = "/gateway/eventReport"

    _TIMEOUT = 60

    def __init__(self):
        self.base_url = os.getenv("REDIS_DETECTION_BASE_URL")

    @interface_log(path=_REPORT_STATUS_URL)
    def report_fail(self):
        try:
            fail_report_data = {"tranCode": "REDIS_FAILED", "eventCode": "REDIS_FAILED"}
            headers = {"Content-Type": "application/json"}
            res = requests.post(
                url=f"{self.base_url}{RedisDetection._REPORT_STATUS_URL}",
                json=fail_report_data,
                headers=headers,
                verify=False,
                timeout=RedisDetection._TIMEOUT,
            )
            return res.status_code, "REDIS_FAILED"
        except Exception as e:
            # redis上报与主流程无关，不中断主流程
            logger.error(f"post redis detection fail, {e}")
            return -1, ""

    @interface_log(path=_HEAT_BEAT_URL)
    def report_heart_beat(self, redis_node_ip):
        try:
            report_data = {"ip": redis_node_ip, "moduleCode": "REDIS_USABILITY"}
            headers = {"Content-Type": "application/json"}
            res = requests.post(
                url=f"{self.base_url}{RedisDetection._HEAT_BEAT_URL}",
                json=report_data,
                headers=headers,
                verify=False,
                timeout=RedisDetection._TIMEOUT,
            )
            return res.status_code, redis_node_ip
        except Exception as e:
            # redis上报与主流程无关，不中断主流程
            logger.error(f"post redis detection fail, {e}")
            return -1, ""


def report_redis_fail(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception:
            record_alarm(
                "redis", "redis", "ERROR", str(e)
            )  # @override(jiuwen) added alarm logging
            if os.getenv("REDIS_DETECTION_BASE_URL"):
                executor.submit(RedisDetection().report_fail)
            raise  # 重新抛出异常，不吞掉

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


class RedisUtils:
    """
    Redis operation tool class.
    """

    # Class variable used to store connection pool instances
    _connection_pool = None
    _cluster: RedisCluster = None

    def __init__(self, config: RedisConfig):
        try:
            # During initialization, the system checks whether the connection pool exists. If the connection pool
            # does not exist, the system creates the connection pool.
            ssl_mode = config.ssl_mode
            if config.mode == RedisMode.CLUSTER.value:
                # 集群配置
                if RedisUtils._cluster is None:
                    if str(ssl_mode).lower() == "false":
                        # 关闭ssl认证
                        RedisUtils._cluster = self._get_redis_cluster_without_ssl(
                            config
                        )
                    else:
                        RedisUtils._cluster = self._get_redis_cluster_with_ssl(config)
                self._redis = RedisUtils._cluster
            else:
                # 单服务配置
                if RedisUtils._connection_pool is None:
                    if str(ssl_mode).lower() == "false":
                        # 关闭ssl认证
                        RedisUtils._connection_pool = (
                            self._get_connection_pool_without_ssl(config)
                        )
                    else:
                        RedisUtils._connection_pool = (
                            self._get_connection_pool_with_ssl(config)
                        )
                self._redis = redis.Redis(connection_pool=RedisUtils._connection_pool)
            # 清理掉内存中的密码信息
            config.passwd = ""
            config.ssl_passwd = ""
            self._redis.ping()
        except redis.ConnectionError as e:
            record_alarm(
                "redis", "redis", "ERROR", f"failed to connect to the redis. {e}"
            )  # @override(jiuwen) added alarm logging
            logger.error(f"failed to connect to the redis: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e
        except Exception as e:
            record_alarm(
                "redis", "redis", "ERROR", f"failed to connect to the redis. {e}"
            )  # @override(jiuwen) added alarm logging
            logger.error(f"An error occurred while initializing the Redis.: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
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
            startup_nodes=RedisUtils._get_startup_nodes_of_redis_cluster(config),
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
                "redis", "redis", "ERROR", f"_get_startup_nodes_of_redis_cluster. {e}"
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
            startup_nodes=RedisUtils._get_startup_nodes_of_redis_cluster(config),
            password=config.passwd,
            decode_responses=False,
            socket_timeout=config.socket_timeout,
            socket_connect_timeout=config.socket_connect_timeout,
            max_connections=config.max_connections,
        )

    @staticmethod
    def get_cluster_nodes():
        return RedisUtils._cluster.get_nodes() if RedisUtils._cluster else []

    @report_redis_fail
    def list_append(self, key, *values, ttl):
        """
        向列表尾部添加一个或多个值
        """
        try:
            self._redis.expire(key, ttl)
            return self._redis.rpush(key, *values)
        except Exception as e:
            logger.error(f"Error adding element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_insert(self, key, index, value):
        """
        在列表的指定位置插入值
        """
        try:
            return self._redis.linsert(key, "AFTER", index, value)
        except Exception as e:
            logger.error(f"error inserting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_remove(self, key, count, value):
        """
        从列表中移除元素
        """
        try:
            return self._redis.lrem(key, count, value)
        except Exception as e:
            logger.error(f"Error removing element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_REMOVE_ELEMENTS_FAILED.code,
                StatusCode.REDIS_REMOVE_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_set(self, key, index, value):
        """
        设置列表在指定位置的值
        """
        try:
            self._redis.lset(key, index, value)
            return True
        except redis.exceptions.RedisError as e:
            logger.error(f"Error setting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SET_SPECIFIED_ELEMENTS_FAILED.code,
                StatusCode.REDIS_SET_SPECIFIED_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_get(self, key, start=0, end=-1):
        """
        获取列表中一段元素
        """
        try:
            return self._redis.lrange(key, start, end)
        except Exception as e:
            logger.error(f"Error getting list: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_contain(self, key):
        """
        确认是否存在某个key
        """
        try:
            return self._redis.exists(key)
        except Exception as e:
            logger.error(f"Error getting list: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LIST_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_pop(self, key):
        """
        从列表头部弹出元素
        """
        try:
            return self._redis.lpop(key)
        except Exception as e:
            logger.error(f"Error ejecting element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_POP_ELEMENTS_FAILED.code,
                StatusCode.REDIS_POP_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_tail_pop(self, key):
        """
        从列表尾部弹出元素, 栈
        """
        try:
            return self._redis.rpop(key)
        except Exception as e:
            logger.error(f"Error ejecting tail element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_POP_ELEMENTS_FAILED.code,
                StatusCode.REDIS_POP_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def list_length(self, key):
        """
        获取列表长度
        """
        try:
            return self._redis.llen(key)
        except Exception as e:
            logger(f"获取列表长度时出错: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_LENGTH_ELEMENTS_FAILED.code,
                StatusCode.REDIS_GET_LENGTH_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def get(self, key):
        """
        根据k获取Redis中的元素
        """
        try:
            return self._redis.get(key)
        except Exception as e:
            logger.error(f"Error getting element from redis, the key is {key}")
            raise JiuWenBaseException(
                StatusCode.REDIS_GET_ELEMENT_FAILED.code,
                StatusCode.REDIS_GET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def set(
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
            return self._redis.set(key, value, ex, None, nx)
        except Exception as e:
            logger.error(f"Error setting element to redis, the key is {key}, {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_SET_ELEMENT_FAILED.code,
                StatusCode.REDIS_SET_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def delete(self, key):
        """
        从Redis中的删除元素
        """
        try:
            return self._redis.delete(key)
        except Exception as e:
            logger.error(f"Error deleting element from redis, the key is {key}")
            raise JiuWenBaseException(
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.code,
                StatusCode.REDIS_DELETE_ELEMENT_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def ping(self):
        """
        从Redis中的删除元素
        """
        try:
            return self._redis.ping()
        except Exception as e:
            logger.error("Error ping redis")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e

    @report_redis_fail
    def ltrim(self, name: str, start: int, end: int = -1):
        """
        从Redis中的删除元素
        """
        try:
            return self._redis.ltrim(name, start, end)
        except Exception as e:
            logger.error("Error ltrim redis")
            raise JiuWenBaseException(
                StatusCode.REDIS_SERVICE_NOT_FOUND.code,
                StatusCode.REDIS_SERVICE_NOT_FOUND.errmsg,
            ) from e

    @report_redis_fail
    def expire(self, key: str, ttl: int):
        """
        从Redis中刷新时间
        """
        try:
            self._redis.expire(key, ttl)
        except Exception as e:
            logger.error(f"Error expire element: {e}")
            raise JiuWenBaseException(
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.code,
                StatusCode.REDIS_INSERT_ELEMENTS_FAILED.errmsg,
            ) from e

    @report_redis_fail
    def get_by_prefix(self, prefix: str) -> Dict[str, Union[str]]:
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
                for key in client.scan_iter(match=pattern):
                    value = client.get(key)
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
                    cursor, keys = client.scan(cursor=cursor, match=pattern, count=100)
                    for key in keys:
                        value = client.get(key)
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
    def delete_by_prefix(self, prefix: str) -> int:
        """
        根据key的前缀批量获取value值
        """
        try:
            client = self._redis()
            deleted_count = 0
            pattern = f"{prefix}*"

            # 分批删除，避免一次性删除大量键带来的性能问题
            batch_size = 1000
            total_batch = 0

            while True:
                if hasattr(client, "scan_iter"):
                    # redis-py 2.6+ 使用 scan_iter
                    keys_batch = []
                    for key in client.scan_iter(match=pattern):
                        keys_batch.append(key)
                        if len(keys_batch) >= batch_size:
                            break

                    if not keys_batch:
                        break

                    # 使用管道删除这一批键
                    pipe = client.pipeline()
                    for key in keys_batch:
                        pipe.delete(key)
                    pipe_result = pipe.execute()
                    deleted_count += sum(batch if batch else 0 for batch in pipe_result)

                    # 如果这批获取的键少于批大小，说明已经遍历完毕
                    if len(keys_batch) < batch_size:
                        break
                else:
                    # 兼容集群模式
                    cursor = b"0"
                    while True:
                        cursor, keys_batch = client.scan(
                            cursor=cursor, match=pattern, count=batch_size
                        )
                        if not keys_batch:
                            break

                        # 使用管道删除这批键
                        pipe = client.pipeline()
                        for key in keys_batch:
                            pipe.delete(key)
                        pipe_result = pipe.execute()
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
    def mget(self, keys: List[str]) -> List[Optional[Union[str]]]:
        """
        批量获取多个键的值
        """
        try:
            client = self._redis()
            values = client.mget(keys)
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


def get_redis_instance(redis_config: Optional[RedisConfig] = None):
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

    return RedisUtils(redis_config)


def check_redis_cluster_connection():
    # 集群模式
    nodes = RedisUtils.get_cluster_nodes()
    if not nodes:
        logger.warning("can not get redis cluster in cluster mode")
        return
    for node in nodes:
        node_data = f"{node.host}" + (
            f":{node.port}"
            if os.getenv("REDIS_DETECT_REPORT_PORT", "false").lower() == "true"
            else ""
        )
        try:
            conn = RedisUtils._cluster.get_redis_connection(node)

            conn.ping()
            RedisDetection().report_heart_beat(node_data)
        except Exception as e:
            # 单个机器报错，跳过继续检查
            logger.error(f"check error {e}")


def check_redis_connection(client: RedisUtils, check_period: int):
    node_id = str(uuid.uuid4())
    logger.info(f"{node_id} start check redis connection")
    try:
        # 获取分布式锁
        if not client.set(
            REDIS_CHECK_LOCK_KEY,
            node_id,
            max(check_period - REDIS_CHECK_TIME_DIFF, REDIS_CHECK_TIME_MIN),
            True,
        ):
            logger.info(f"{node_id} skip check redis connection")
            return
        if (
            os.getenv("DATASOURCE_REDIS_MODE", RedisMode.SINGLE.value)
            == RedisMode.SINGLE.value
        ):
            # 单机模式
            client.ping()
            RedisDetection().report_heart_beat(
                os.getenv("DATASOURCE_REDIS_HOST")
                + (
                    ":" + os.getenv("DATASOURCE_REDIS_PORT")
                    if os.getenv("REDIS_DETECT_REPORT_PORT", "false").lower() == "true"
                    else ""
                )
            )
        else:
            check_redis_cluster_connection()
    except Exception as e:
        logger.error(f"check error {e}")
    logger.info(f"{node_id} finish check redis connection")


def start_heart_check():
    if not os.getenv("REDIS_DETECTION_BASE_URL"):
        logger.warning("not config redis detection, skip")
        return
    scheduler = sched.scheduler(time.time, time.sleep)
    client = get_redis_instance()
    check_period = int(os.getenv("REDIS_CHECK_PERIOD_TIME", 180))

    def periodic_check():
        check_redis_connection(client, check_period)
        scheduler.enter(check_period, 1, periodic_check, ())

    # 启动后不立即启动任务
    scheduler.enter(check_period, 1, periodic_check, ())

    thread = threading.Thread(target=scheduler.run, daemon=True)
    thread.start()
    logger.info("start redis period check task")


start_heart_check()
