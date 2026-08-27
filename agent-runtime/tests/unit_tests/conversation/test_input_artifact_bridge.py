import hashlib
from types import SimpleNamespace

import pytest

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    reset_conversation_execution_context,
    set_conversation_execution_context,
)
from agent_runtime.conversation.input_artifact_bridge import (
    ConversationInputArtifact,
    InputArtifactBridge,
    InputArtifactPreparationError,
)


class RecordingFs:
    def __init__(self, result=None):
        self.calls = []
        self._result = result if result is not None else SimpleNamespace(is_ok=lambda: True)

    async def write_file(self, path, content, **kwargs):
        self.calls.append((path, content, kwargs))
        return self._result


class RecordingOperation:
    def __init__(self, fs):
        self._fs = fs

    def fs(self):
        return self._fs


class BytesStorage:
    def __init__(self, data=None, error=None):
        self.data = data
        self.error = error
        self.object_keys = []

    async def get_object_bytes(self, object_key):
        self.object_keys.append(object_key)
        if self.error is not None:
            raise self.error
        return self.data


def _artifact(data=b"report contents", file_name="report.pdf"):
    return ConversationInputArtifact(
        objectKey=(
            "conversation-inputs/"
            "244210e48437b6556980a70249a99369934a352429034cef9d7bd253b3bf2c01/"
            "21a3230e03772a58aff1b3709a9e232850916337e1fba95c434076b6668c6e08/"
            "04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb/"
            "00000000-0000-0000-0000-000000000001/report.pdf"
        ),
        fileName=file_name,
        size=len(data),
        checksum=hashlib.sha256(data).hexdigest(),
    )


def _context():
    return ConversationExecutionContext.create(
        ConversationIdentity("project", "workspace", "user", "conversation", "execution"),
        "/workspace",
    )


@pytest.mark.parametrize("object_key", [
    " ",
    "https://files.example/report.pdf",
    "conversation-inputs/../report.pdf",
    "conversation-inputs/project/\x00report.pdf",
    "/conversation-inputs/project/report.pdf",
    "conversation-inputs\\project\\report.pdf",
])
def test_input_artifact_rejects_untrusted_object_keys(object_key):
    with pytest.raises(ValueError, match="object key"):
        ConversationInputArtifact(
            objectKey=object_key,
            fileName="report.pdf",
            size=1,
            checksum="0" * 64,
        )


@pytest.mark.parametrize("file_name", ["../report.pdf", "nested/report.pdf", "nested\\report.pdf"])
def test_input_artifact_rejects_path_shaped_file_names(file_name):
    with pytest.raises(ValueError, match="file name"):
        _artifact(file_name=file_name)


@pytest.mark.asyncio
async def test_bridge_writes_verified_bytes_to_server_derived_input_path_only():
    artifact = _artifact()
    storage = BytesStorage(b"report contents")
    fs = RecordingFs()
    bridge = InputArtifactBridge(storage, lambda: RecordingOperation(fs), max_file_size=1024)
    token = set_conversation_execution_context(_context())
    try:
        paths = await bridge.prepare([artifact])
    finally:
        reset_conversation_execution_context(token)

    assert storage.object_keys == [artifact.object_key]
    assert paths == [fs.calls[0][0]]
    assert paths[0].startswith("/workspace/")
    assert "/input/" in paths[0]
    assert paths[0].endswith("/report.pdf")
    assert fs.calls == [(paths[0], b"report contents", {
        "mode": "bytes", "prepend_newline": False, "append_newline": False,
    })]


@pytest.mark.asyncio
@pytest.mark.parametrize("artifact,data", [
    (_artifact(), b"truncated"),
    (_artifact(data=b"report contents"), b"different bytes"),
])
async def test_bridge_rejects_incomplete_or_checksum_mismatched_download_before_writing(artifact, data):
    fs = RecordingFs()
    bridge = InputArtifactBridge(BytesStorage(data), lambda: RecordingOperation(fs), max_file_size=1024)
    token = set_conversation_execution_context(_context())
    try:
        with pytest.raises(InputArtifactPreparationError, match="size|checksum"):
            await bridge.prepare([artifact])
    finally:
        reset_conversation_execution_context(token)
    assert fs.calls == []


@pytest.mark.asyncio
async def test_bridge_rejects_oversized_or_failed_download_without_writing():
    artifact = _artifact(data=b"12345")
    fs = RecordingFs()
    oversized = InputArtifactBridge(BytesStorage(b"12345"), lambda: RecordingOperation(fs), max_file_size=4)
    failed = InputArtifactBridge(BytesStorage(error=RuntimeError("storage unavailable")), lambda: RecordingOperation(fs))
    token = set_conversation_execution_context(_context())
    try:
        with pytest.raises(InputArtifactPreparationError, match="size limit"):
            await oversized.prepare([artifact])
        with pytest.raises(InputArtifactPreparationError, match="download"):
            await failed.prepare([artifact])
    finally:
        reset_conversation_execution_context(token)
    assert fs.calls == []


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "failed_result",
    [
        SimpleNamespace(is_ok=lambda: False, error=lambda: "remote write rejected"),
        SimpleNamespace(code=199003, message="permission denied", data=None),
    ],
    ids=["callable-is-ok", "aio-status-code"],
)
async def test_bridge_fails_when_sandbox_write_does_not_succeed(failed_result):
    bridge = InputArtifactBridge(
        BytesStorage(b"report contents"),
        lambda: RecordingOperation(RecordingFs(failed_result)),
    )
    token = set_conversation_execution_context(_context())
    try:
        with pytest.raises(InputArtifactPreparationError, match="write"):
            await bridge.prepare([_artifact()])
    finally:
        reset_conversation_execution_context(token)
