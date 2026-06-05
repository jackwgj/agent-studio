#  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
import json
import logging
import os
import time
from logging.handlers import RotatingFileHandler

from jiuwen.common.configs.base import config
from jiuwen.common.configs.env_constants import (
    JIUWEN_LOG_PATH_KEY,
    JIUWEN_LOG_MAX_BYTES_KEY,
    JIUWEN_LOG_BACKUP_COUNT_KEY,
)

DEFAULT_LOG_MAX_BYTES = 100 * 1024 * 1024  # 100MB
ENV_BACKUP_COUNT = int(os.environ.get(JIUWEN_LOG_BACKUP_COUNT_KEY, "10"))

ALARM_LOG_ENABLE = "true" == os.environ.get("ALARM_LOG_ENABLE")

alarm_logger = None


class SafeRotatingFileHandler(RotatingFileHandler):
    """
    for safe RotatingFileHandler.
    """

    def __init__(self, filename, *args, **kwargs):
        # 暂不处理
        filename = f"{filename}"
        super().__init__(filename, *args, **kwargs)
        # 设置文件的初始权限为 640
        os.chmod(self.baseFilename, 0o640)

    def doRollover(self):
        # Rotate the file first.
        # 日志文件（正在记录）权限为640，日志文件（记录完毕或者已经归档）权限为440
        RotatingFileHandler.doRollover(self)
        for i in range(self.backupCount, 0, -1):
            sfn = f"{self.baseFilename}.{i}"
            if os.path.exists(sfn):
                os.chmod(sfn, 0o440)
        os.chmod(self.baseFilename, 0o640)


class SingletAlarmLogger:
    __alarm_instance: logging.Logger = None
    __process_port = 0

    def __init__(self):
        if SingletAlarmLogger.__alarm_instance is not None:
            return

        log_config = config["logging"]
        if log_config is None:
            return
        log_path = os.path.join(
            os.environ.get(JIUWEN_LOG_PATH_KEY, log_config.get("log_path")), "alarm"
        )
        if not os.path.exists(os.path.dirname(log_path)):
            # 日志文件目录权限为750
            os.makedirs(os.path.dirname(log_path), mode=0o750)

        log_file = os.path.join(
            log_path, f"alarm_{SingletAlarmLogger.__process_port}.log"
        )
        SingletAlarmLogger.__alarm_instance = SingletAlarmLogger._get_logger(log_file)

    @classmethod
    def set_process_port(cls, port):
        cls.__process_port = port
        if ALARM_LOG_ENABLE:
            global alarm_logger
            alarm_logger = SingletAlarmLogger.get_logger()

    @classmethod
    def _get_logger(cls, log_file):
        max_bytes = int(os.environ.get(JIUWEN_LOG_MAX_BYTES_KEY, DEFAULT_LOG_MAX_BYTES))

        alarm_logger_instance = logging.getLogger("alarm_log")
        alarm_logger_instance.setLevel(logging.INFO)

        if not os.path.exists(os.path.dirname(log_file)):
            # 日志文件目录权限为750
            os.makedirs(os.path.dirname(log_file), mode=0o750)
        file_handler = SafeRotatingFileHandler(
            filename=log_file,
            maxBytes=max_bytes,
            backupCount=ENV_BACKUP_COUNT,
            encoding="utf-8",
        )
        formatter = logging.Formatter("%(message)s")
        file_handler.setFormatter(formatter)
        alarm_logger_instance.addHandler(file_handler)
        return alarm_logger_instance

    @classmethod
    def get_logger(cls):
        if cls.__alarm_instance is None:
            SingletAlarmLogger()
        return cls.__alarm_instance


def record_alarm(source: str, resource: str, serverity="ERROR", cause=""):
    if alarm_logger is not None:
        alarm = json.dumps(
            {
                "source": source,
                "resource": resource,
                "severity": serverity,
                "cause": cause,
                "create_time": int(time.time() * 1000),
            },
            ensure_ascii=False,
        )
        alarm_logger.info(alarm)
