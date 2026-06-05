import os
import threading

from cachetools import TTLCache
from jiuwen.common.log.base import logger


class ThreadSafeCallbackTTLCache:
    def __init__(
        self, maxsize: int = 1000, ttl: float = 600, cleanup_interval: float = 60
    ):
        """
        :param maxsize: 缓存的最大容量
        :param ttl: 键值对的存活时间（秒）
        :param cleanup_interval: 清理线程的间隔时间（秒）
        """
        self._cache = TTLCache(maxsize=maxsize, ttl=ttl)
        self._callbacks = {}
        self._lock = threading.Lock()
        self._cleanup_interval = cleanup_interval
        self._stop_event = threading.Event()

        # 启动守护线程
        self._start_daemon_cleanup()

    def _start_daemon_cleanup(self):
        self._cleaner_thread = threading.Thread(
            target=self._cleanup_expired, daemon=True, name="TTLCacheCleaner"
        )
        self._cleaner_thread.start()

    def _cleanup_expired(self):
        """后台线程：定期清理过期键"""
        logger.info("TTLCacheCleaner start.")
        try:
            while not self._stop_event.is_set():
                with self._lock:
                    # 显式调用 .expire() 触发清理
                    for k, v in self._cache.expire():
                        if k in self._callbacks:
                            callback, args, kwargs = self._callbacks[k]
                            callback(k, v, *args, **kwargs)
                # 等待下一次清理（可被 stop() 提前中断）
                self._stop_event.wait(self._cleanup_interval)
        except Exception as e:
            logger.warning(f"TTLCacheCleaner error: {e}")

    def get(self, key):
        """获取键值（线程安全）"""
        with self._lock:
            return self._cache.get(key)

    def set(self, key, value, callback=None, *args, **kwargs):
        """设置键值（线程安全）"""
        with self._lock:
            self._cache[key] = value
            if callback:
                self._callbacks[key] = (callback, args, kwargs)

    def delete_without_callback(self, key):
        """删除键（线程安全）"""
        with self._lock:
            if key in self._cache:
                del self._cache[key]
            if key in self._callbacks:
                del self._callbacks[key]

    def stop(self):
        """停止清理线程"""
        self._stop_event.set()
        self._cleaner_thread.join()


callback_ttl_cache = ThreadSafeCallbackTTLCache(
    maxsize=int(os.environ.get("CALLBACK_TTL_CACHE_MAXSIZE", "5000")),
    ttl=int(os.environ.get("CALLBACK_TTL_CACHE_TTL", "600")),
)
