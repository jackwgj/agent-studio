import logging

from agent_runtime.context.request_context import _request_ctx
from openjiuwen.core.common.logging import get_session_id

COMMON_LOG_FORMAT = (
    "%(asctime)s|%(log_type)s|%(filename)s:%(lineno)d|%(funcName)s|"
    "%(trace_id)s|%(execution_id)s|%(request_id)s|%(levelname)s|%(message)s"
)
PERFORMANCE_LOG_FORMAT = (
    "%(asctime)s|%(log_type)s|%(trace_id)s|%(execution_id)s|%(request_id)s|"
    "%(levelname)s|%(message)s"
)
DEFAULT_LOG_FORMAT = "%(asctime)s|%(log_type)s|%(trace_id)s|%(levelname)s|%(message)s"

_INSTALLED = False
_FORMATTER_PATCH_INSTALLED = False


def get_log_format(log_type: str) -> str:
    if log_type == "performance":
        return PERFORMANCE_LOG_FORMAT
    return COMMON_LOG_FORMAT


def install_log_formatter_patch() -> None:
    """Make openjiuwen default backend choose formats by logger type."""
    global _FORMATTER_PATCH_INSTALLED
    if _FORMATTER_PATCH_INSTALLED:
        return

    from openjiuwen.core.common.logging.default.default_impl import DefaultLogger

    def get_formatter(self):
        return logging.Formatter(
            get_log_format(self.log_type), datefmt="%Y-%m-%d %H:%M:%S"
        )

    DefaultLogger._get_formatter = get_formatter
    _FORMATTER_PATCH_INSTALLED = True


def install_request_id_log_record_factory() -> None:
    """Add request_id to stdlib LogRecords used by openjiuwen formatters."""
    global _INSTALLED
    if _INSTALLED:
        return

    previous_factory = logging.getLogRecordFactory()

    def record_factory(*args, **kwargs):
        record = previous_factory(*args, **kwargs)
        if not hasattr(record, "trace_id"):
            record.trace_id = get_session_id()
        if not hasattr(record, "execution_id"):
            record.execution_id = _request_ctx.get().execution_id
        if not hasattr(record, "request_id"):
            record.request_id = _request_ctx.get().request_id
        return record

    logging.setLogRecordFactory(record_factory)
    _INSTALLED = True
