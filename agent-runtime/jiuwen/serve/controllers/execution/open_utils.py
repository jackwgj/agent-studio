# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""This module contains open utilities."""

import mmap
import os
import pickle
import stat
import struct
import sys
import threading
import time
from typing import Optional, Any

from jiuwen.common.configs.env_constants import (
    CACHE_FILE_PREFIX_KEY,
    CACHE_TTL_SECONDS_KEY,
    MAX_AGENT_CACHE_NUM_KEY,
    MAX_CACHE_DATA_SIZE_KEY,
    MAX_IR_CACHE_NUM_KEY,
    MAX_WORKFLOW_CACHE_NUM_KEY,
    IR_CACHE_ENABLE_KEY,
)
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.utils.utils import safe_json_loads_raise_exception
from jiuwen.serve.common.logger.request_logger import log_function_timing


def _run_async_in_sync(coro):
    """在同步上下文中运行异步协程，避免嵌套事件循环冲突。

    当已有运行中的事件循环时（如在 async 测试中），直接在新 loop 中运行；
    否则使用 asyncio.run()。

    Args:
        coro: 要运行的协程对象

    Returns:
        协程的返回值
    """
    try:
        # 检查是否有正在运行的事件循环
        asyncio.get_running_loop()
        # 有 running loop，创建新 loop 运行
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        try:
            return loop.run_until_complete(coro)
        finally:
            loop.close()
            asyncio.set_event_loop(None)
    except RuntimeError:
        # No running event loop, use asyncio.run()
        return asyncio.run(coro)


# 跨平台文件锁辅助函数
def _file_lock(fd, lock_type):
    """跨平台文件锁"""
    try:
        if sys.platform == "win32":
            import msvcrt

            if lock_type in ["shared", "exclusive"]:
                msvcrt.locking(fd, msvcrt.LK_NBLCK, 1)
            elif lock_type == "unlock":
                msvcrt.locking(fd, msvcrt.LK_UNLCK, 1)
        else:
            import fcntl

            if lock_type == "shared":
                fcntl.flock(fd, fcntl.LOCK_SH)
            elif lock_type == "exclusive":
                fcntl.flock(fd, fcntl.LOCK_EX)
            elif lock_type == "unlock":
                fcntl.flock(fd, fcntl.LOCK_UN)
    except (OSError, ImportError) as e:
        logger.error(f"file lock error: {e}")


MAX_IR_CACHE_NUM = int(os.getenv(MAX_IR_CACHE_NUM_KEY, str(100)))
MAX_WORKFLOW_CACHE_NUM = int(os.getenv(MAX_WORKFLOW_CACHE_NUM_KEY, str(100)))
MAX_AGENT_CACHE_NUM = int(os.getenv(MAX_AGENT_CACHE_NUM_KEY, str(100)))
MAX_AGENT_GROUP_CACHE_NUM = int(os.getenv("MAX_AGENT_GROUP_CACHE_NUM", str(100)))
MAX_INTENT_RULE_CACHE_NUM = int(os.getenv("MAX_AGENT_RULE_CACHE_NUM", str(100)))

if sys.platform == "win32":
    default_prefix = os.path.join(os.getcwd(), "jiuwen_cache")
else:
    default_prefix = "/tmp/jiuwen_cache"
CACHE_FILE_PREFIX = os.getenv(CACHE_FILE_PREFIX_KEY, default_prefix)
CACHE_TTL_SECONDS = int(os.getenv(CACHE_TTL_SECONDS_KEY, "86400"))
MAX_DATA_SIZE = int(os.getenv(MAX_CACHE_DATA_SIZE_KEY, str(2 * 1024 * 1024)))  # 2MB


class PickleSharedMemoryCache:
    """基于pickle的共享内存缓存 - 支持任意Python对象"""

    def __init__(self, cache_name="default", capacity=100, max_data_size=None):
        self.cache_name = cache_name
        self.capacity = capacity
        self.max_data_size = max_data_size or MAX_DATA_SIZE

        # 所有进程共享同一个文件
        self.cache_file = f"{CACHE_FILE_PREFIX}_{cache_name}_{capacity}.mmap"

        self.metadata_size = 128
        self.slot_size = self.max_data_size + self.metadata_size
        self.total_size = 1024 + capacity * self.slot_size

        self._init_shared_mmap()
        self.lock = threading.Lock()

        self.local_stats = {
            "process_id": os.getpid(),
            "puts": 0,
            "gets": 0,
            "hits": 0,
            "pickle_errors": 0,
        }

        logger.info(
            f"Process {os.getpid()} initialized pickle cache: {self.cache_file}"
        )

    def __del__(self):
        self.close()

    @staticmethod
    def _safe_hash(key: str) -> int:
        """safe hash"""
        hash_value = hash(key)
        return hash_value if hash_value >= 0 else hash_value + 2**64

    def get_ttl(self, key: str) -> Optional[int]:
        """获取缓存项的剩余生存时间（秒）"""
        if not key:
            return None

        index = self._hash_key(key)
        offset = self._get_slot_offset(index)

        try:
            with self.lock:
                _file_lock(self.fd, "shared")

                self.mmap.seek(offset)

                # 读取元信息
                try:
                    key_hash = struct.unpack("Q", self.mmap.read(8))[0]
                    timestamp = struct.unpack("d", self.mmap.read(8))[0]
                    data_length = struct.unpack("I", self.mmap.read(4))[0]
                    key_length = struct.unpack("H", self.mmap.read(2))[0]
                    is_valid = struct.unpack("H", self.mmap.read(2))[0]
                except struct.error:
                    return None

                # 验证数据有效性
                expected_hash = self._safe_hash(key)
                if is_valid != 1 or key_hash != expected_hash or data_length == 0:
                    return None

                # 跳过剩余元信息
                self.mmap.read(104)

                # 验证key
                if key_length > 0:
                    stored_key = self.mmap.read(key_length).decode(
                        "utf-8", errors="ignore"
                    )
                    if stored_key != key:
                        return None

                # 计算剩余时间
                current_time = time.time()
                elapsed_time = current_time - timestamp
                remaining_time = CACHE_TTL_SECONDS - elapsed_time

                # 如果已过期，返回0
                if remaining_time <= 0:
                    return 0

                # 返回剩余秒数（向上取整）
                return int(remaining_time) + 1

        except Exception as e:
            logger.warning(
                f"Failed to get TTL for key {key}: {e}",
                simple_log="Failed to get TTL for key",
            )
            return None
        finally:
            try:
                _file_lock(self.fd, "unlock")
            except Exception as e:
                logger.error(
                    f"Failed to unlock file lock: {e}",
                    simple_log="Failed to unlock file lock",
                )

    def get(self, key: str) -> Optional[Any]:
        """获取任意Python对象"""
        if not key:
            return None

        self.local_stats["gets"] += 1
        index = self._hash_key(key)
        offset = self._get_slot_offset(index)

        try:
            with self.lock:
                _file_lock(self.fd, "shared")

                self.mmap.seek(offset)

                # 读取元信息
                try:
                    key_hash = struct.unpack("Q", self.mmap.read(8))[0]
                    timestamp = struct.unpack("d", self.mmap.read(8))[0]
                    data_length = struct.unpack("I", self.mmap.read(4))[0]
                    key_length = struct.unpack("H", self.mmap.read(2))[0]
                    is_valid = struct.unpack("H", self.mmap.read(2))[0]
                except struct.error:
                    return None

                # 验证数据
                expected_hash = self._safe_hash(key)
                if is_valid != 1 or key_hash != expected_hash or data_length == 0:
                    return None

                # 检查TTL
                if time.time() - timestamp > CACHE_TTL_SECONDS:
                    return None

                # 跳过剩余元信息
                self.mmap.read(104)

                # 验证key
                if key_length > 0:
                    stored_key = self.mmap.read(key_length).decode(
                        "utf-8", errors="ignore"
                    )
                    if stored_key != key:
                        return None

                # 读取并反序列化数据
                if data_length > 0:
                    pickled_data = self.mmap.read(data_length)
                    try:
                        # 直接pickle反序列化，支持任意Python对象！
                        obj = pickle.loads(pickled_data)
                        self.local_stats["hits"] += 1
                        return obj
                    except (pickle.PickleError, EOFError, ValueError) as e:
                        logger.warning(f"Pickle deserialize error for key {key}: {e}")
                        self.local_stats["pickle_errors"] += 1
                        return None

                return None

        except Exception as e:
            logger.warning(f"Failed to get pickle cache for key {key}: {e}")
            return None
        finally:
            try:
                _file_lock(self.fd, "unlock")
            except Exception as e:
                logger.error(f"Failed to unlock file lock: {e}")

    def put(self, key: str, value: Any):
        """存储任意Python对象"""
        if not key or value is None:
            return

        try:
            key_bytes = key.encode("utf-8")

            # 直接pickle序列化 - 支持任意对象！
            try:
                pickled_data = pickle.dumps(value, protocol=pickle.HIGHEST_PROTOCOL)
            except (pickle.PickleError, TypeError) as e:
                logger.warning(
                    f"Cannot pickle object for key {key}: {e}",
                    simple_log="Cannot pickle object for key",
                )
                self.local_stats["pickle_errors"] += 1
                return

            # 大小验证
            user_data_size = len(key_bytes) + len(pickled_data)
            if user_data_size > self.max_data_size:
                logger.warning(
                    f"Pickled data too large: {key}, "
                    f"size: {user_data_size // 1024}KB, max: {self.max_data_size // 1024}KB",
                    simple_log=f"Pickled data too large, "
                    f"size: {user_data_size // 1024}KB, max: {self.max_data_size // 1024}KB",
                )
                return

            self.local_stats["puts"] += 1
            index = self._hash_key(key)
            offset = self._get_slot_offset(index)

            with self.lock:
                _file_lock(self.fd, "exclusive")

                # 清除槽位
                self.mmap.seek(offset)
                self.mmap.write(b"\x00" * self.slot_size)

                # 写入新数据
                self.mmap.seek(offset)
                self.mmap.write(struct.pack("Q", self._safe_hash(key)))
                self.mmap.write(struct.pack("d", time.time()))
                self.mmap.write(struct.pack("I", len(pickled_data)))
                self.mmap.write(struct.pack("H", len(key_bytes)))
                self.mmap.write(struct.pack("H", 1))  # valid flag
                self.mmap.write(b"\x00" * 104)  # padding

                # 写入key和pickled数据
                self.mmap.write(key_bytes)
                self.mmap.write(pickled_data)

                # 填充剩余空间
                remaining = (
                    self.slot_size
                    - self.metadata_size
                    - len(key_bytes)
                    - len(pickled_data)
                )
                if remaining > 0:
                    self.mmap.write(b"\x00" * remaining)
                else:
                    raise JiuWenBaseException(
                        StatusCode.SERIALIZATION_ERROR.code, "Not enough space."
                    )

                # 立即刷新到磁盘
                self.mmap.flush()

                logger.info(
                    f"Process {os.getpid()} pickled and cached: {key}, "
                    f"original_type: {type(value).__name__}",
                    simple_log=f"Process pickled and cached, "
                    f"original_type: {type(value).__name__}",
                )

        except Exception as e:
            logger.error(
                f"Failed to put pickle cache for key {key}: {e}",
                simple_log="Failed to put pickle cache for key",
            )
        finally:
            try:
                _file_lock(self.fd, "unlock")
            except Exception as e:
                logger.error(f"Failed to unlock file lock: {e}")

    def close(self):
        """清理缓存"""
        # 关闭 mmap
        if getattr(self, "mmap", None) is not None:
            try:
                self.mmap.flush()
                self.mmap.close()
            except Exception as e:
                logger.error(f"Failed to close mmap: {e}")
            finally:
                self.mmap = None

        # 关闭 fd
        if getattr(self, "fd", None) is not None:
            try:
                os.close(self.fd)
            except Exception as e:
                logger.error(f"Failed to close file descriptor: {e}")
            finally:
                self.fd = None

    def _init_shared_mmap(self):
        """初始化共享mmap文件"""
        try:
            # 检查文件是否需要创建
            if (
                not os.path.exists(self.cache_file)
                or os.path.getsize(self.cache_file) != self.total_size
            ):
                self._create_shared_cache_file()

            # 映射文件
            self.file_mode = stat.S_IRUSR | stat.S_IWUSR
            self.fd = os.open(self.cache_file, os.O_RDWR, self.file_mode)
            self.mmap = mmap.mmap(self.fd, self.total_size)

            # 验证/写入文件头
            if not self._validate_header():
                self._write_header()

        except Exception as e:
            # 初始化失败，关闭已打开的资源
            self.close()

            logger.error(f"Failed to initialize pickle cache: {e}")
            raise

    def _create_shared_cache_file(self):
        """线程安全创建共享文件"""
        lock_file = f"{self.cache_file}.lock"

        try:
            with open(lock_file, "w") as lock_fd:
                _file_lock(lock_fd.fileno(), "exclusive")

                # 再次检查（可能被其他进程创建了）
                if (
                    os.path.exists(self.cache_file)
                    and os.path.getsize(self.cache_file) == self.total_size
                ):
                    return

                # 创建文件
                logger.info(
                    f"Process {os.getpid()} creating pickle cache file: {self.cache_file}"
                )
                with open(self.cache_file, "wb") as f:
                    f.write(b"\x00" * self.total_size)

        finally:
            try:
                if os.path.exists(lock_file):
                    os.remove(lock_file)
            except Exception as e:
                logger.error(f"Failed to remove lock file: {e}")

    def _validate_header(self) -> bool:
        """验证文件头"""
        try:
            self.mmap.seek(0)
            magic = self.mmap.read(8)
            if magic != b"JWPICKLE":
                return False

            capacity, max_size, ttl, version = struct.unpack("IIII", self.mmap.read(16))
            return (
                capacity == self.capacity
                and max_size == self.max_data_size
                and ttl == CACHE_TTL_SECONDS
                and version == 1
            )
        except Exception as e:
            logger.error(f"Failed to validate header: {e}")
            return False

    def _write_header(self):
        """写入文件头"""
        self.mmap.seek(0)
        self.mmap.write(b"JWPICKLE")  # 8字节魔数
        self.mmap.write(
            struct.pack("IIII", self.capacity, self.max_data_size, CACHE_TTL_SECONDS, 1)
        )  # 版本号
        self.mmap.write(b"\x00" * (1024 - 24))
        self.mmap.flush()

    def _hash_key(self, key: str) -> int:
        # 直接使用_safe_hash的结果
        return self._safe_hash(key) % self.capacity

    def _get_slot_offset(self, index: int) -> int:
        return 1024 + index * self.slot_size


class ConcurrentLimitedQueue:
    """兼容接口，使用pickle缓存"""

    def __init__(self, capacity=None, cache_name="default"):
        self.capacity = capacity or MAX_IR_CACHE_NUM
        self.cache_name = cache_name
        self._pickle_cache = None
        self.lock = threading.Lock()

    def __del__(self):
        """清理缓存"""
        if self._pickle_cache is not None:
            self._pickle_cache.close()

    def put(self, key, value):
        """put key"""
        cache = self._get_cache()
        cache.put(key, value)

    async def aput(self, key, value):
        """async put key"""
        return self.put(key, value)

    def get(self, key):
        """get key"""
        cache = self._get_cache()
        return cache.get(key)

    async def aget(self, key):
        """async get key"""
        return self.get(key)

    def pop(self, key):
        """pop key"""
        logger.info(f"pop {key} from {self.cache_name}")

    async def apop(self, key):
        """async pop key"""
        return self.pop(key)

    def update_capacity(self, cache_num=None):
        """update capacity"""
        if cache_num and cache_num != self.capacity:
            with self.lock:
                self.capacity = cache_num

                # 清理旧缓存
                if self._pickle_cache is not None:
                    self._pickle_cache.close()
                self._pickle_cache = PickleSharedMemoryCache(
                    cache_name=self.cache_name,
                    capacity=self.capacity,
                    max_data_size=MAX_DATA_SIZE,
                )

    def _get_cache(self):
        if self._pickle_cache is None:
            with self.lock:
                if self._pickle_cache is None:
                    self._pickle_cache = PickleSharedMemoryCache(
                        cache_name=self.cache_name,
                        capacity=self.capacity,
                        max_data_size=MAX_DATA_SIZE,
                    )
        return self._pickle_cache


# 使用pickle缓存
cache_ir_queue = ConcurrentLimitedQueue(capacity=MAX_IR_CACHE_NUM, cache_name="ir")
cache_workflow_queue = ConcurrentLimitedQueue(
    capacity=MAX_WORKFLOW_CACHE_NUM, cache_name="workflow"
)
cache_agent_queue = ConcurrentLimitedQueue(
    capacity=MAX_AGENT_CACHE_NUM, cache_name="agent"
)
cache_agent_group_queue = ConcurrentLimitedQueue(
    capacity=MAX_AGENT_GROUP_CACHE_NUM, cache_name="agent_group"
)
cache_intent_rule_queue = ConcurrentLimitedQueue(
    capacity=MAX_INTENT_RULE_CACHE_NUM, cache_name="intent_rule"
)


@log_function_timing
async def async_ir_load(path: str) -> dict:
    """
    异步加载IR内容，支持任意Python对象缓存。

    功能描述：
    此函数用于异步从指定路径加载IR内容。首先检查缓存中是否存在该内容，如果存在则直接返回缓存数据；如果不存在，则从OBS中加载并缓存。

    Args:
        path (str): IR内容的路径。

    Returns:
        dict: 包含IR数据的字典，其中包含`ir_path`和`is_published`字段。

    Raises:
        JiuWenBaseException: 如果JSON加载失败，抛出此异常。

    Notes:
        - 函数会记录加载过程的日志信息。
        - 如果环境变量`IR_CACHE_ENABLE_KEY`设置为`true`且IR已发布，则会将数据缓存。
    """

    logger.info("Async Loading IR content from %s", path)

    local_ir_data = _load_ir_from_local_file(path)
    if local_ir_data is not None:
        return local_ir_data

    cache_ir_queue.update_capacity()
    ir_value = await cache_ir_queue.aget(path)
    if ir_value:
        logger.info(
            "Cache HIT! Process %d async got pickled data: %s", os.getpid(), path
        )
        return ir_value

    logger.info("Cache MISS! Process %d async loading from OBS: %s", os.getpid(), path)
    from agent_runtime.serve.apis.orchestration import _load_ir_json

    ir_data = await _load_ir_json(path)

    ir_data["ir_path"] = path
    ir_data["is_published"] = is_ir_published(path)

    if (
        str(os.environ.get(IR_CACHE_ENABLE_KEY, "false")).lower() == "true"
        and ir_data["is_published"]
    ):
        await cache_ir_queue.aput(path, ir_data)
        logger.info("Process %d async pickled and cached data: %s", os.getpid(), path)

    return ir_data


@log_function_timing
def ir_load(path: str) -> dict:
    """
    加载IR内容，支持任意Python对象缓存。

    功能描述：
    此函数用于从指定路径加载IR内容。首先检查缓存中是否存在该内容，如果存在则直接返回缓存数据；如果不存在，则从OBS中加载并缓存。

    Args:
        path (str): IR内容的路径。

    Returns:
        dict: 包含IR数据的字典，其中包含`ir_path`和`is_published`字段。

    Raises:
        JiuWenBaseException: 如果JSON加载失败，抛出此异常。

    Notes:
        - 函数会记录加载过程的日志信息。
        - 如果环境变量`IR_CACHE_ENABLE_KEY`设置为`true`且IR已发布，则会将数据缓存。
    """

    logger.info("Loading IR content from %s", path)

    local_ir_data = _load_ir_from_local_file(path)
    if local_ir_data is not None:
        return local_ir_data

    cache_ir_queue.update_capacity()
    ir_value = cache_ir_queue.get(path)
    if ir_value:
        logger.info("Cache HIT! Process %d got pickled data: %s", os.getpid(), path)
        return ir_value

    logger.info("Cache MISS! Process %d loading from OBS: %s", os.getpid(), path)
    # 使用开源层的存储提供者（统一存储层）
    from agent_runtime.storage import get_storage_provider

    provider = get_storage_provider()
    ir_json_str = _run_async_in_sync(provider.get_content(path))

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
        str(os.environ.get(IR_CACHE_ENABLE_KEY, "false")).lower() == "true"
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


def _resolve_local_ir_path(path: str) -> str | None:
    """Resolve a local IR file path for test and local-debug scenarios."""
    if not path:
        return None

    if os.path.isfile(path):
        return path

    local_ir_root = os.environ.get("JIUWEN_LOCAL_IR_ROOT", "")
    if local_ir_root:
        candidate = os.path.join(local_ir_root, path)
        if os.path.isfile(candidate):
            return candidate

    return None


def _load_ir_from_local_file(path: str) -> dict | None:
    """Load IR from local filesystem when a direct file path is provided."""
    local_path = _resolve_local_ir_path(path)
    if local_path is None:
        return None

    logger.info("Loading IR content from local file: %s", local_path)
    with open(local_path, "r", encoding="utf-8") as file:
        ir_json_str = file.read()

    try:
        ir_data = safe_json_loads_raise_exception(ir_json_str)
    except ValueError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.IR_DATA_JSON_LOAD_FAILED.code,
            message=StatusCode.IR_DATA_JSON_LOAD_FAILED.errmsg,
        ) from e

    ir_data["ir_path"] = local_path
    ir_data["is_published"] = False
    return ir_data


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


@log_function_timing
def deserialize_object(serialized_data: bytes):
    """
    使用pickle反序列化给定的字节对象。

    功能描述：
    此函数接收一个字节对象，并尝试使用pickle进行反序列化。如果反序列化失败，则抛出异常。

    Args:
        serialized_data (bytes): 需要反序列化的字节对象。

    Returns:
        object: 反序列化后的Python对象。

    Raises:
        JiuWenBaseException: 如果反序列化失败，抛出此异常。

    Notes:
        - 函数会记录反序列化过程的日志信息。
        - 使用pickle反序列化存在安全风险，应确保数据来源安全。
    """

    try:
        # Handle legacy format: storage layer JSON-serialized pickle bytes
        if (
            isinstance(serialized_data, str)
            and serialized_data.startswith("b'")
            and serialized_data.endswith("'")
        ):
            # Extract content between b' and '
            inner = serialized_data[2:-1]
            # Use ast.literal_eval to parse the byte string representation
            import ast

            serialized_data = ast.literal_eval("b'" + inner + "'")
        elif isinstance(serialized_data, str):
            serialized_data = serialized_data.encode("latin-1")
        return pickle.loads(serialized_data)
    except Exception as e:
        raise JiuWenBaseException(
            error_code=StatusCode.DESERIALIZATION_ERROR.code,
            message=StatusCode.DESERIALIZATION_ERROR.errmsg,
        ) from e
