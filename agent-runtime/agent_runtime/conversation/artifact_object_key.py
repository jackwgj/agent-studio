"""Trusted object-key derivation for formal conversation artifacts."""

from __future__ import annotations

import hashlib
import re
import unicodedata
import uuid

from agent_runtime.conversation.execution_context import ConversationIdentity


_ARTIFACT_PREFIX = "conversation-artifacts"
_UNSAFE_NAME = re.compile(r"[^A-Za-z0-9._-]+")
_REPEATED_DOT = re.compile(r"\.{2,}")
_MAX_SAFE_NAME_BYTES = 140


def conversation_artifact_prefix(identity: ConversationIdentity) -> str:
    """Return the execution prefix derived only from trusted identity fields."""
    segments = (
        identity.project_id,
        identity.workspace_id,
        identity.user_id,
        identity.conversation_id,
        identity.execution_id,
    )
    return "/".join((
        _ARTIFACT_PREFIX,
        *(_path_key(value) for value in segments),
        "",
    ))


def create_conversation_artifact_object_key(
    identity: ConversationIdentity,
    file_name: str,
    *,
    artifact_id: uuid.UUID | str | None = None,
) -> str:
    """Create a collision-resistant key without trusting a path-shaped name."""
    identifier = _artifact_uuid(artifact_id)
    safe_name = _safe_file_name(file_name)
    return f"{conversation_artifact_prefix(identity)}{identifier}-{safe_name}"


def _path_key(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def _artifact_uuid(value: uuid.UUID | str | None) -> uuid.UUID:
    if value is None:
        return uuid.uuid4()
    if isinstance(value, uuid.UUID):
        return value
    try:
        return uuid.UUID(value)
    except (AttributeError, TypeError, ValueError) as error:
        raise ValueError("artifact id must be a UUID") from error


def _safe_file_name(value: str) -> str:
    if not isinstance(value, str) or not value.strip():
        return "artifact"
    normalized = unicodedata.normalize("NFKC", value.replace("\\", "/"))
    basename = normalized.rsplit("/", 1)[-1]
    safe = _UNSAFE_NAME.sub("_", basename)
    safe = _REPEATED_DOT.sub(".", safe).strip("._-")
    if not safe:
        safe = "artifact"
    encoded = safe.encode("utf-8")
    if len(encoded) <= _MAX_SAFE_NAME_BYTES:
        return safe

    suffix = ""
    if "." in safe:
        stem, extension = safe.rsplit(".", 1)
        if 0 < len(extension) <= 16:
            suffix = f".{extension}"
            safe = stem
    budget = _MAX_SAFE_NAME_BYTES - len(suffix.encode("utf-8"))
    truncated = safe.encode("utf-8")[:budget].decode("utf-8", errors="ignore")
    return f"{truncated.rstrip('._-') or 'artifact'}{suffix}"
