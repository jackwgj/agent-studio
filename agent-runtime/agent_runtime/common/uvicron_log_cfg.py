"""Uvicorn log configuration for OLE."""

LOG_FORMAT = "%(asctime)s | %(levelname)-7s | %(name)s | %(message)s"


class UvicornLogConfig:
    """Minimal uvicorn log config builder."""

    def __init__(self, log_level: str = "info"):
        self.log_level = log_level

    def to_dict(self) -> dict:
        return {
            "version": 1,
            "disable_existing_loggers": False,
            "formatters": {
                "default": {
                    "()": "uvicorn.logging.DefaultFormatter",
                    "fmt": LOG_FORMAT,
                    "use_colors": True,
                },
                "access": {
                    "()": "uvicorn.logging.AccessFormatter",
                    "fmt": LOG_FORMAT,
                },
            },
            "handlers": {
                "default": {
                    "formatter": "default",
                    "class": "logging.StreamHandler",
                    "stream": "ext://sys.stderr",
                },
                "access": {
                    "formatter": "access",
                    "class": "logging.StreamHandler",
                    "stream": "ext://sys.stdout",
                },
            },
            "loggers": {
                "uvicorn.error": {
                    "level": self.log_level.upper(),
                    "handlers": ["default"],
                    "propagate": False,
                },
                "uvicorn.access": {
                    "level": self.log_level.upper(),
                    "handlers": ["access"],
                    "propagate": False,
                },
            },
        }
