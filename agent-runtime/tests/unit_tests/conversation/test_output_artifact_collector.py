import hashlib
import importlib

import pytest

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    reset_conversation_execution_context,
    set_conversation_execution_context,
)


class RecordingOutputSource:
    """Remote-sandbox boundary used by the collector; never touches Runtime FS."""

    def __init__(self, entries, contents=None):
        self.entries = entries
        self.contents = contents or {}
        self.scan_roots = []
        self.read_paths = []

    async def scan(self, root):
        self.scan_roots.append(root)
        return self.entries

    async def read_bytes(self, path):
        self.read_paths.append(path)
        return self.contents[path]


def _api():
    """Load the production contract inside each test so RED cases remain visible."""
    module = importlib.import_module(
        "agent_runtime.conversation.output_artifact_collector"
    )
    return (
        module.ConversationOutputCollector,
        module.OutputArtifactCollectionError,
        module.SandboxOutputEntry,
    )


def _context(execution_id="execution-current"):
    return ConversationExecutionContext.create(
        ConversationIdentity(
            "project", "workspace", "user", "conversation", execution_id
        ),
        "/workspace",
    )


async def _collect(source, **limits):
    collector_type, _, _ = _api()
    context = _context()
    token = set_conversation_execution_context(context)
    try:
        return await collector_type(source, **limits).collect(), context
    finally:
        reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_collects_regular_files_from_current_execution_output_only():
    _, _, entry_type = _api()
    context = _context()
    report = str(context.output_dir / "reports" / "result.pdf")
    content = b"%PDF-1.7\nconversation output"
    source = RecordingOutputSource(
        [entry_type(path=report, size=len(content), is_symlink=False)],
        {report: content},
    )

    artifacts, bound_context = await _collect(source)

    assert source.scan_roots == [str(bound_context.output_dir)]
    assert source.read_paths == [report]
    assert len(artifacts) == 1
    assert artifacts[0].sandbox_path == report
    assert artifacts[0].file_name == "result.pdf"
    assert artifacts[0].size == len(content)
    assert artifacts[0].media_type == "application/pdf"
    assert artifacts[0].checksum == hashlib.sha256(content).hexdigest()
    assert artifacts[0].content == content


@pytest.mark.asyncio
@pytest.mark.parametrize("outside_path", [
    "../tmp/secret.txt",
    "../../../../work/secret.txt",
    "/workspace/other-conversation/runs/other/output/result.txt",
])
async def test_rejects_parent_escape_and_paths_outside_current_output(outside_path):
    _, error_type, entry_type = _api()
    context = _context()
    candidate = str(context.output_dir / outside_path) if not outside_path.startswith("/") else outside_path
    source = RecordingOutputSource(
        [entry_type(path=candidate, size=4, is_symlink=False)],
        {candidate: b"data"},
    )

    with pytest.raises(error_type, match="output directory"):
        await _collect(source)
    assert source.read_paths == []


@pytest.mark.asyncio
async def test_rejects_symbolic_links_without_reading_their_targets():
    _, error_type, entry_type = _api()
    context = _context()
    link = str(context.output_dir / "latest.pdf")
    source = RecordingOutputSource(
        [entry_type(path=link, size=12, is_symlink=True)],
        {link: b"outside data"},
    )

    with pytest.raises(error_type, match="symbolic link"):
        await _collect(source)
    assert source.read_paths == []


@pytest.mark.asyncio
async def test_rejects_more_files_than_the_configured_limit_before_reading():
    _, error_type, entry_type = _api()
    context = _context()
    entries = [
        entry_type(
            path=str(context.output_dir / f"result-{index}.txt"),
            size=1,
            is_symlink=False,
        )
        for index in range(3)
    ]
    source = RecordingOutputSource(entries)

    with pytest.raises(error_type, match="file count"):
        await _collect(source, max_files=2)
    assert source.read_paths == []


@pytest.mark.asyncio
async def test_rejects_a_file_larger_than_the_configured_limit_before_reading():
    _, error_type, entry_type = _api()
    context = _context()
    path = str(context.output_dir / "large.pdf")
    source = RecordingOutputSource(
        [entry_type(path=path, size=11, is_symlink=False)],
        {path: b"x" * 11},
    )

    with pytest.raises(error_type, match="file size"):
        await _collect(source, max_file_size=10)
    assert source.read_paths == []


@pytest.mark.asyncio
async def test_rejects_outputs_whose_declared_total_exceeds_the_limit():
    _, error_type, entry_type = _api()
    context = _context()
    entries = [
        entry_type(
            path=str(context.output_dir / f"part-{index}.txt"),
            size=6,
            is_symlink=False,
        )
        for index in range(2)
    ]
    source = RecordingOutputSource(entries)

    with pytest.raises(error_type, match="total size"):
        await _collect(source, max_total_size=10)
    assert source.read_paths == []


@pytest.mark.asyncio
@pytest.mark.parametrize("file_name,content", [
    ("report.docx", b"PK\x03\x04docx"),
    ("report.pdf", b"%PDF-1.7"),
    ("report.xlsx", b"PK\x03\x04xlsx"),
    ("slides.pptx", b"PK\x03\x04pptx"),
    ("chart.png", b"\x89PNG\r\n\x1a\n"),
    ("bundle.zip", b"PK\x03\x04zip"),
    ("transform.py", b"print('ok')\n"),
])
async def test_allows_supported_document_image_archive_and_code_types(file_name, content):
    _, _, entry_type = _api()
    context = _context()
    path = str(context.output_dir / file_name)
    source = RecordingOutputSource(
        [entry_type(path=path, size=len(content), is_symlink=False)],
        {path: content},
    )

    artifacts, _ = await _collect(source)

    assert [artifact.file_name for artifact in artifacts] == [file_name]


@pytest.mark.asyncio
@pytest.mark.parametrize("file_name", ["payload.exe", "library.so"])
async def test_rejects_unsupported_output_types(file_name):
    _, error_type, entry_type = _api()
    context = _context()
    path = str(context.output_dir / file_name)
    source = RecordingOutputSource(
        [entry_type(path=path, size=4, is_symlink=False)],
        {path: b"data"},
    )

    with pytest.raises(error_type, match="file type"):
        await _collect(source)
    assert source.read_paths == []
