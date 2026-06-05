import os

from memory_service.config.settings import LOG_LEVEL, LOG_PATH

logging_config = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "access": {
            "()": "uvicorn.logging.AccessFormatter",
            "fmt": '%(levelprefix)s %(asctime)s %(client_addr)s - "%(request_line)s" %(status_code)s',
        },
        "default": {
            "format": "[%(asctime)s] %(levelname)s [%(name)s:%(lineno)s] %(message)s",
            "datefmt": "%Y-%m-%d %H:%M:%S",
        },
    },
    "handlers": {
        "access_file": {
            "formatter": "access",
            "class": "logging.handlers.RotatingFileHandler",
            "filename": os.path.join(LOG_PATH, "access.log"),
            "maxBytes": 10485760,  # 10MB
            "backupCount": 30,
        },
        "service_file": {
            "formatter": "default",
            "class": "logging.handlers.RotatingFileHandler",
            "filename": os.path.join(LOG_PATH, "memory_service.log"),
            "maxBytes": 10485760,  # 10MB
            "backupCount": 30,
        },
        "console": {
            "formatter": "default",
            "class": "logging.StreamHandler",
            "stream": "ext://sys.stdout",
        },
    },
    "loggers": {
        "uvicorn.access": {
            "handlers": ["access_file"],
            "level": LOG_LEVEL,
            "propagate": False,
        },
        "uvicorn.error": {
            "handlers": ["service_file", "console"],
            "level": LOG_LEVEL,
            "propagate": False,
        },
        "uvicorn.server": {
            "handlers": ["service_file", "console"],
            "level": LOG_LEVEL,
            "propagate": False,
        },
        "root": {
            "handlers": ["service_file", "console"],
            "level": LOG_LEVEL,
            "propagate": False,
        },
    },
}
