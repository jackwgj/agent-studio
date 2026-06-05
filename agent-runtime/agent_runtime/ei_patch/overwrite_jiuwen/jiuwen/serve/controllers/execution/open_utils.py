# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""This module contains open utilities."""

import builtins
import importlib
import io
import os
import pickle
import time
import zipfile
from typing import Any

from cachetools import LRUCache
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.store.async_obs import AsyncOBSUtil
from jiuwen.common.store.async_redis import get_async_redis_instance
from jiuwen.common.store.obs import OBSUtil
from jiuwen.common.store.redis import get_redis_instance
from jiuwen.common.utils.utils import safe_json_loads_raise_exception
from jiuwen.serve.common.logger.request_logger import log_function_timing

MAX_IR_CACHE_NUM = int(os.getenv("MAX_IR_CACHE_NUM", str(100)))
MAX_WORKFLOW_CACHE_NUM = int(os.getenv("MAX_WORKFLOW_CACHE_NUM", str(100)))
MAX_AGENT_GROUP_CACHE_NUM = int(os.getenv("MAX_AGENT_GROUP_CACHE_NUM", str(100)))

CACHE_TTL_SECONDS = int(os.getenv("CACHE_TTL_SECONDS", "3600"))
MEM_CACHE_TTL_SECONDS = int(os.getenv("MEM_CACHE_TTL_SECONDS", "3600"))
MAX_DATA_SIZE = int(os.getenv("MAX_CACHE_DATA_SIZE", str(2 * 1024 * 1024)))  # 2MB
MAX_INTENT_RULE_CACHE_NUM = int(os.getenv("MAX_AGENT_RULE_CACHE_NUM", str(100)))


class EICacheUtils:
    """
    EICacheUtils，使用内存+redis二级存储方式
    capacity: 内存缓存大小，默认100
    should_serialize: 非str类型需要序列化存储
    cache_name: cache字段名，用于生成key
    memory_ttl: 内存超时时间，-1表示永不超时
    redis_ttl: redis超时时间，默认24h
    """

    def __init__(
        self,
        capacity: int,
        should_serialize: bool,
        cache_name: str,
        memory_ttl: int = -1,
        redis_ttl: int = CACHE_TTL_SECONDS,
    ):
        self.memory_cache = LRUCache(capacity)
        self.redis_cache = get_redis_instance()
        self.async_redis_cache = get_async_redis_instance()
        self.should_serialize = should_serialize
        self.cache_name = cache_name
        self.memory_ttl = memory_ttl
        self.redis_ttl = redis_ttl

    async def aput(self, key: str, value: Any):
        """
        异步刷新内存和redis缓存, redis需序列化后存入
        :param key: 缓存key
        :param value: 缓存value
        """
        try:
            unique_key = self._generate_unique_key(key)
            self._update_memory_cache(unique_key, value)
            if self.should_serialize:
                await self.async_redis_cache.set(
                    key=unique_key, value=serialize_object(value), ex=self.redis_ttl
                )
            else:
                await self.async_redis_cache.set(
                    key=unique_key, value=value, ex=self.redis_ttl
                )
            logger.info(
                f"put {key} in {self.cache_name} memory and redis, "
                f"memory size {self.memory_cache.currsize}/{self.memory_cache.maxsize}"
            )
        except Exception as e:
            logger.error(f"cache put error, exception {e}", exc_info=True)

    def put(self, key: str, value: Any):
        """
        刷新内存和redis缓存, redis需序列化后存入
        :param key: 缓存key
        :param value: 缓存value
        """
        try:
            unique_key = self._generate_unique_key(key)
            self._update_memory_cache(unique_key, value)
            if self.should_serialize:
                self.redis_cache.set(
                    key=unique_key, value=serialize_object(value), ex=self.redis_ttl
                )
            else:
                self.redis_cache.set(key=unique_key, value=value, ex=self.redis_ttl)
            logger.info(
                f"put {key} in {self.cache_name} memory and redis, "
                f"memory size {self.memory_cache.currsize}/{self.memory_cache.maxsize}"
            )
        except Exception as e:
            logger.error(f"cache put error, exception {e}", exc_info=True)

    async def aget(self, key: str, should_refresh_ttl: bool = False) -> Any:
        """
        根据key读元素，顺序memory->redis
        :param key: 缓存key
        :param should_refresh_ttl: 是否刷新redis时间
        :return: 缓存value
        """
        try:
            unique_key = self._generate_unique_key(key)
            value = self._get_from_memory_cache(unique_key)
            if value is None:
                value = await self.async_redis_cache.get(unique_key)
            else:
                logger.info(f"memory hit {key} in {self.cache_name} memory")
                # 更新内存缓存有效期
                self._update_memory_cache(unique_key, value)
                return value

            # redis命中
            if value is not None:
                # redis未失效内存失效，刷新内存和redis时间
                if should_refresh_ttl:
                    await self.async_redis_cache.expire(unique_key, self.redis_ttl)
                value = deserialize_object(value) if self.should_serialize else value
                self._update_memory_cache(unique_key, value)
                logger.info(
                    f"redis hit, put {key} in {self.cache_name} memory, "
                    f"size {self.memory_cache.currsize}/{self.memory_cache.maxsize}"
                )
            return value
        except Exception as e:
            logger.error(f"cache get error, exception {e}", exc_info=True)
            return None

    def get(self, key: str, should_refresh_ttl: bool = False) -> Any:
        """
        根据key读元素，顺序memory->redis
        :param key: 缓存key
        :param should_refresh_ttl: 是否刷新redis时间
        :return: 缓存value
        """
        try:
            unique_key = self._generate_unique_key(key)
            value = self._get_from_memory_cache(unique_key)
            if value is None:
                value = self.redis_cache.get(unique_key)
            else:
                logger.info(f"memory hit {key} in {self.cache_name} memory")
                return value

            # redis命中
            if value is not None:
                # redis未失效内存失效，刷新内存和redis时间
                if should_refresh_ttl:
                    self.redis_cache.expire(unique_key, self.redis_ttl)
                value = deserialize_object(value) if self.should_serialize else value
                self._update_memory_cache(unique_key, value)
                logger.info(
                    f"redis hit, put {key} in {self.cache_name} memory, "
                    f"size {self.memory_cache.currsize}/{self.memory_cache.maxsize}"
                )
            return value
        except Exception as e:
            logger.error(f"cache get error, exception {e}", exc_info=True)
            return None

    async def apop(self, key: str):
        """
        删除元素
        :param key: 缓存key
        """
        try:
            unique_key = self._generate_unique_key(key)
            if self.memory_cache.get(unique_key) is not None:
                self.memory_cache.pop(unique_key)
            value = await self.async_redis_cache.get(unique_key)
            if value is not None:
                await self.async_redis_cache.delete(key=unique_key)
            logger.info(f"pop {key} from {self.cache_name}")
        except Exception as e:
            logger.error(f"cache pop error, exception {e}", exc_info=True)

    def pop(self, key: str):
        """
        删除元素
        :param key: 缓存key
        """
        try:
            unique_key = self._generate_unique_key(key)
            if self.memory_cache.get(unique_key) is not None:
                self.memory_cache.pop(unique_key)
            if self.redis_cache.get(unique_key) is not None:
                self.redis_cache.delete(key=unique_key)
            logger.info(f"pop {key} from {self.cache_name}")
        except Exception as e:
            logger.error(f"cache pop error, exception {e}", exc_info=True)

    def _update_memory_cache(self, key: str, value: Any):
        """
        刷新内存缓存
        :param key: 原key
        :param : 值value
        """
        expire_time = (
            int((time.time() + self.memory_ttl) * 1000) if self.memory_ttl > 0 else -1
        )
        self.memory_cache[key] = {"expire_time": expire_time, "data": value}

    def _get_from_memory_cache(self, key: str) -> Any:
        """
        从内存读取缓存
        :param key: 原key
        :return: 值value
        """
        cached_value = self.memory_cache.get(key)
        if cached_value is not None:
            current_time = int(time.time() * 1000)

            # expire_time == -1表示永不过期
            if cached_value["expire_time"] <= 0:
                return cached_value["data"]

            # 缓存命中但是超期
            if current_time > cached_value.get("expire_time"):
                self.memory_cache.pop(key)
                return None
            else:
                return cached_value["data"]
        return None

    def _generate_unique_key(self, key: str) -> str:
        """
        生成key
        :param key: 原key
        """
        return f"ei:engine:{self.cache_name}:{key}"


# 使用pickle缓存
cache_ir_queue = EICacheUtils(
    capacity=MAX_IR_CACHE_NUM,
    should_serialize=True,
    cache_name="ir",
    memory_ttl=MEM_CACHE_TTL_SECONDS,
)
cache_workflow_queue = EICacheUtils(
    capacity=MAX_WORKFLOW_CACHE_NUM, should_serialize=True, cache_name="workflow"
)
cache_agent_queue = EICacheUtils(
    capacity=MAX_WORKFLOW_CACHE_NUM, should_serialize=True, cache_name="agent"
)
cache_agent_group_queue = EICacheUtils(
    capacity=MAX_AGENT_GROUP_CACHE_NUM, should_serialize=True, cache_name="agent_group"
)
cache_intent_rule_queue = EICacheUtils(
    capacity=MAX_INTENT_RULE_CACHE_NUM, should_serialize=True, cache_name="intent_rule"
)


@log_function_timing
async def async_ir_load(path: str) -> dict:
    """异步加载IR内容，支持任意Python对象缓存"""
    logger.info("Async Loading IR content from %s", path)

    ir_value = await cache_ir_queue.aget(path)
    if ir_value:
        logger.info(
            "Cache HIT! Process %d async got pickled data: %s", os.getpid(), path
        )
        return ir_value

    logger.info("Cache MISS! Process %d async loading from OBS: %s", os.getpid(), path)
    ir_json_bytes = await AsyncOBSUtil.get_content(object_key=path)
    ir_json_str = ir_json_bytes.decode("utf-8")

    try:
        ir_data = safe_json_loads_raise_exception(ir_json_str)
    except ValueError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.IR_DATA_JSON_LOAD_FAILED.code,
            message=StatusCode.IR_DATA_JSON_LOAD_FAILED.errmsg,
        ) from e

    ir_data["ir_path"] = path
    ir_data["is_published"] = is_ir_published(path)

    if (
        str(os.environ.get("IR_CACHE_ENABLE", "false")).lower() == "true"
        and ir_data["is_published"]
    ):
        await cache_ir_queue.aput(path, ir_data)
        logger.info("Process %d async pickled and cached data: %s", os.getpid(), path)

    return ir_data


@log_function_timing
async def async_download_skills(ir_data: dict, work_dir: str):
    if not ir_data.get("configs", {}).get("skills", {}):
        return
    skill_dir = ir_data["configs"]["skills"]["skill_dir"]
    skill_local_path_prefix = f"{work_dir}/{skill_dir}"

    # 确保skills目录存在
    os.makedirs(skill_local_path_prefix, exist_ok=True)

    # 获取skills配置
    skills_config = ir_data["configs"]["skills"]
    skill_list = skills_config.get("skill_info", [])

    # 遍历每个skill并下载
    for skill in skill_list:
        skill_name = skill.get("name")
        skill_path = skill.get("skill_path")

        if not skill_name or not skill_path:
            logger.warning(f"Skill missing name or path, skipping: {skill}")
            continue

        # 构建本地保存路径
        local_zip_path = os.path.join(skill_local_path_prefix, f"{skill_name}.zip")

        try:
            # 下载skill zip文件
            logger.info(f"Downloading skill {skill_name} from {skill_path}")
            await AsyncOBSUtil.download_to_file(
                object_key=skill_path, local_path=local_zip_path
            )

            # 解压zip文件到工作目录 (兼容 Windows 畸形反斜杠)
            with zipfile.ZipFile(local_zip_path, "r") as zip_ref:
                for member in zip_ref.infolist():
                    # 将文件名中的反斜杠 \ 替换为正斜杠 /
                    member.filename = member.filename.replace("\\", "/")
                    # 单个提取并写入到目标目录
                    zip_ref.extract(member, skill_local_path_prefix)

            # 解压成功后删除zip文件
            try:
                os.remove(local_zip_path)
                logger.info(f"Successfully deleted zip file: {local_zip_path}")
            except Exception as e:
                logger.warning(f"Failed to delete zip file {local_zip_path}: {e}")

            logger.info(f"Successfully downloaded and extracted skill: {skill_name}")

        except Exception as e:
            logger.error(f"Failed to download/extract skill {skill_name}: {e}")
            # 可以选择继续下载其他skill或者raise
            continue


@log_function_timing
def ir_load(path: str) -> dict:
    """加载IR内容，支持任意Python对象缓存"""
    logger.info("Loading IR content from %s", path)

    ir_value = cache_ir_queue.get(path)
    if ir_value:
        logger.info("Cache HIT! Process %d got cached data: %s", os.getpid(), path)
        return ir_value

    logger.info("Cache MISS! Process %d loading from OBS: %s", os.getpid(), path)
    ir_json_str: str = OBSUtil.get_content(object_key=path).decode("utf-8")

    try:
        ir_data = safe_json_loads_raise_exception(ir_json_str)
    except ValueError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.IR_DATA_JSON_LOAD_FAILED.code,
            message=StatusCode.IR_DATA_JSON_LOAD_FAILED.errmsg,
        ) from e

    ir_data["ir_path"] = path
    ir_data["is_published"] = is_ir_published(path)

    if (
        str(os.environ.get("IR_CACHE_ENABLE", "false")).lower() == "true"
        and ir_data["is_published"]
    ):
        cache_ir_queue.put(path, ir_data)
        logger.info("Process %d pickled and cached data: %s", os.getpid(), path)

    return ir_data


def is_ir_published(ir_path: str):
    """根据ir_path判断是否为已发布应用"""
    # ir_path格式为workflow/ir/{workflow_id}/{workflow_id}_{timestamp}.json
    # 获取文件名称：{workflow_id}_{timestamp}
    ir_file_name = ir_path.split("/")[-1].split(".")[0]
    # 是否包含下划线
    if "_" not in ir_file_name:
        return False
    # 是否后缀为时间戳
    file_name_suffix = ir_file_name.split("_")[-1]
    if file_name_suffix.isdigit() and len(file_name_suffix) == 13:
        return True
    return False


@log_function_timing
def serialize_object(obj):
    """
    Serializes the given object to bytes using pickle.
    """
    try:
        return pickle.dumps(obj, protocol=pickle.HIGHEST_PROTOCOL)
    except Exception as e:
        raise JiuWenBaseException(
            error_code=StatusCode.SERIALIZATION_ERROR.code,
            message=StatusCode.SERIALIZATION_ERROR.errmsg,
        ) from e


class RestrictedUnpickler(pickle.Unpickler):
    _class_cache = {}

    def find_class(self, module, name):
        # 内置类型直接返回
        if module == "builtins":
            return getattr(builtins, name)

        # 自定义类型此处暂不限制，开放所有自定义结构体
        cache_key = (module, name)
        if cache_key not in self._class_cache:
            module_obj = importlib.import_module(module)
            self._class_cache[cache_key] = getattr(module_obj, name)
        return self._class_cache[cache_key]


@log_function_timing
def deserialize_object(serialized_data: bytes):
    """
    Deserializes the given bytes object using pickle.
    """
    try:
        return RestrictedUnpickler(io.BytesIO(serialized_data)).load()
    except Exception as e:
        raise JiuWenBaseException(
            error_code=StatusCode.DESERIALIZATION_ERROR.code,
            message=StatusCode.DESERIALIZATION_ERROR.errmsg,
        ) from e
