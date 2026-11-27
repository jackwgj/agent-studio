"""Vision-only input resolution for the opt-in native WorkflowCard."""

import base64
from contextvars import ContextVar
from dataclasses import dataclass
import hashlib
import logging
import re

import httpx

from agent_runtime.conversation.image_reference import (
    ConversationImage,
    PrivateImageError,
    REFERENCE_PREFIX,
    current_image_catalog,
)
from jiuwen.extension.workflow.lazy_workflow import LazyWorkflow


_URL_TTL_SECONDS = 120
_MIME_SIGNATURES = {"image/png": b"\x89PNG\r\n\x1a\n", "image/jpeg": b"\xff\xd8\xff"}


@dataclass(slots=True, repr=False)
class _PrivateVisionCall:
    ref: str
    image: ConversationImage
    encoded_image: str = ""


_selected: ContextVar[_PrivateVisionCall | None] = ContextVar("private_vision_input", default=None)
_DATA_URI = re.compile(r"data:image/[^;,\s]+;base64,[a-z0-9+/=]+", re.IGNORECASE)
_SIGNED_URL = re.compile(r"https?://[^\s\"'<>]*[?&]X-Amz-(?:Algorithm|Signature|Credential)=[^\s\"'<>]*", re.IGNORECASE)


def is_private_image_call() -> bool:
    return _selected.get() is not None


def redact_private_vision(value):
    """Redact an observation copy; the actual model payload remains unchanged."""
    if isinstance(value, dict):
        return {key: redact_private_vision(item) for key, item in value.items()}
    if isinstance(value, (list, tuple)):
        return [redact_private_vision(item) for item in value]
    if isinstance(value, str):
        safe = _SIGNED_URL.sub("[private image URL]", _DATA_URI.sub("[private image data]", value))
        selected = _selected.get()
        return safe.replace(selected.encoded_image, "[private image data]") if selected and selected.encoded_image else safe
    return value


class _PrivateImageLogFilter(logging.Filter):
    def filter(self, record):
        # HTTP request logs and signing debug logs can expose portions of the URL
        # separately, so suppress that low-level output for this call only.
        return not is_private_image_call()


class _PrivateImageModelLogger:
    def __init__(self, logger):
        self._logger = logger

    def __getattr__(self, name):
        target = getattr(self._logger, name)
        if name not in {"debug", "info", "warning", "error", "critical", "exception", "log"}:
            return target

        def protected(*args, **kwargs):
            if not is_private_image_call():
                return target(*args, **kwargs)
            return None

        return protected


def install_private_image_log_protection() -> None:
    """Use the SDK's logger registration API for providers that log full messages."""
    from openjiuwen.core.common.logging.manager import LogManager

    llm_logger = LogManager.get_logger("llm")
    if not isinstance(llm_logger, _PrivateImageModelLogger):
        LogManager.register_logger("llm", _PrivateImageModelLogger(llm_logger))
    for name in ("httpx", "httpcore", "httpcore.connection", "httpcore.http11", "httpcore.http2",
                 "openai._base_client", "botocore.auth", "botocore.signers", "botocore.endpoint",
                 "aiobotocore.signers"):
        logger = logging.getLogger(name)
        if not any(isinstance(item, _PrivateImageLogFilter) for item in logger.filters):
            logger.addFilter(_PrivateImageLogFilter())


class ConversationImageWorkflow(LazyWorkflow):
    """Invoke the same Workflow graph without inserting credentials into its inputs."""

    async def invoke(self, inputs, session, context=None, **kwargs):
        ref = inputs.get("image_source") if isinstance(inputs, dict) else None
        if isinstance(ref, str) and ref.startswith(("http://", "https://")):
            return await super().invoke(inputs, session, context=context, **kwargs)
        catalog = current_image_catalog.get()
        if catalog is None or catalog.workflow_id != self.card.id:
            raise PrivateImageError("图片工作流调用缺少本轮授权")
        selected = catalog.select(ref)
        install_private_image_log_protection()
        call = _PrivateVisionCall(ref, selected)
        marker = _selected.set(call)
        try:
            output = await super().invoke(inputs, session, context=context, **kwargs)
            if not call.encoded_image:
                raise PrivateImageError("图片工作流未读取所选图片，请检查视觉节点的图片输入")
            if hasattr(output, "model_copy") and hasattr(output, "result"):
                return output.model_copy(update={"result": redact_private_vision(output.result)})
            return redact_private_vision(output)
        finally:
            _selected.reset(marker)


async def resolve_private_image(ref: str) -> str | None:
    """Keep the URL local to this function; always fail closed on private reads."""
    selected = _selected.get()
    if selected is None:
        if ref.startswith(REFERENCE_PREFIX):
            raise PrivateImageError("图片引用不在当前工作流调用范围内")
        return None
    if ref != selected.ref:
        raise PrivateImageError("图片引用与本次选择不匹配")
    image = selected.image
    try:
        from storage.object_storage import get_storage_provider

        storage = get_storage_provider()
        signed_url = await storage.get_presigned_url(image.object_key, _URL_TTL_SECONDS)
        async with httpx.AsyncClient(timeout=20, trust_env=False, follow_redirects=False) as client:
            async with client.stream("GET", signed_url) as response:
                response.raise_for_status()
                data = bytearray()
                async for chunk in response.aiter_bytes(chunk_size=64 * 1024):
                    if len(data) + len(chunk) > image.size:
                        raise ValueError("invalid length")
                    data.extend(chunk)
        if (len(data) != image.size or not data.startswith(_MIME_SIGNATURES[image.media_type])
                or hashlib.sha256(data).hexdigest() != image.checksum):
            raise ValueError("invalid content")
        selected.encoded_image = base64.b64encode(data).decode("ascii")
        return f"data:{image.media_type};base64,{selected.encoded_image}"
    except Exception:
        # Do not record exceptions that can embed the full signed URL.
        pass
    raise PrivateImageError("私有图片读取失败，请重试或重新上传")
