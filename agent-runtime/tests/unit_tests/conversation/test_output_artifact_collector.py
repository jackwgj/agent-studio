import hashlib
import importlib
import json
from types import SimpleNamespace

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


@pytest.fixture(autouse=True)
def active_execution():
    token = set_conversation_execution_context(_context())
    try:
        yield
    finally:
        reset_conversation_execution_context(token)


class RecordingShell:
    def __init__(self, stdout="[]", exit_code=0, ok=True):
        self.result = SimpleNamespace(
            is_ok=lambda: ok,
            data=SimpleNamespace(stdout=stdout, exit_code=exit_code),
        )
        self.calls = []

    async def execute_cmd(self, command, **kwargs):
        self.calls.append((command, kwargs))
        return self.result


@pytest.mark.asyncio
async def test_absent_output_does_not_chdir_to_missing_directory():
    module = importlib.import_module("agent_runtime.conversation.output_artifact_collector")
    ctx = _context()
    token = set_conversation_execution_context(ctx)
    calls = []

    async def execute(_command, **kwargs):
        calls.append(kwargs)
        # The AIO wrapper runs cd before the scan script gets to check existence.
        missing_cwd = kwargs.get("cwd") == str(ctx.output_dir)
        return SimpleNamespace(code=0, data=SimpleNamespace(
            exit_code=1 if missing_cwd else 0, stdout="" if missing_cwd else "[]",
            stderr="cd: No such file or directory" if missing_cwd else "",
        ))

    try:
        source = module.RemoteSandboxOutputSource(SimpleNamespace(shell=lambda: SimpleNamespace(execute_cmd=execute)))
        assert await source.scan(str(ctx.output_dir)) == []
        assert len(calls) == 1
    finally:
        reset_conversation_execution_context(token)


class RecordingRemoteFs:
    def __init__(self, content=b"data", ok=True):
        self.result = SimpleNamespace(
            is_ok=lambda: ok,
            data=SimpleNamespace(content=content),
        )
        self.calls = []

    async def read_file(self, path, **kwargs):
        self.calls.append((path, kwargs))
        return self.result


class RecordingRemoteOperation:
    def __init__(self, shell, fs):
        self._shell = shell
        self._fs = fs

    def shell(self):
        return self._shell

    def fs(self):
        return self._fs


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
async def test_baseline_filters_unchanged_files_but_keeps_created_and_modified():
    collector_type, _, entry_type = _api()
    context = _context()
    unchanged = str(context.output_dir / "unchanged.txt")
    modified = str(context.output_dir / "modified.txt")
    created = str(context.output_dir / "created.txt")
    source = RecordingOutputSource(
        [entry_type(unchanged, 4, False), entry_type(modified, 3, False), entry_type(created, 3, False)],
        {unchanged: b"same", modified: b"new", created: b"new"},
    )
    baseline = {
        "unchanged.txt": hashlib.sha256(b"same").hexdigest(),
        "modified.txt": hashlib.sha256(b"old").hexdigest(),
        "deleted.txt": hashlib.sha256(b"old").hexdigest(),
    }

    artifacts = await collector_type(source).collect(baseline=baseline)

    assert [artifact.file_name for artifact in artifacts] == ["modified.txt", "created.txt"]


@pytest.mark.asyncio
async def test_historical_outputs_do_not_consume_per_turn_limits():
    collector_type, _, entry_type = _api()
    context = _context()
    entries = []
    contents = {}
    baseline = {}
    for index in range(3):
        path = str(context.output_dir / f"historical-{index}.txt")
        content = f"old-{index}".encode()
        entries.append(entry_type(path, len(content), False))
        contents[path] = content
        baseline[f"historical-{index}.txt"] = hashlib.sha256(content).hexdigest()
    created = str(context.output_dir / "created.txt")
    entries.append(entry_type(created, 3, False))
    contents[created] = b"new"

    artifacts = await collector_type(
        RecordingOutputSource(entries, contents),
        max_files=1,
        max_total_size=3,
    ).collect(baseline=baseline)

    assert [artifact.file_name for artifact in artifacts] == ["created.txt"]


@pytest.mark.asyncio
async def test_snapshot_allows_persistent_history_beyond_per_turn_limits():
    collector_type, _, entry_type = _api()
    context = _context()
    entries = []
    contents = {}
    for index in range(3):
        path = str(context.output_dir / f"historical-{index}.txt")
        content = f"old-{index}".encode()
        entries.append(entry_type(path, len(content), False))
        contents[path] = content

    snapshot = await collector_type(
        RecordingOutputSource(entries, contents),
        max_files=1,
        max_total_size=1,
    ).snapshot()

    assert set(snapshot) == {
        "historical-0.txt", "historical-1.txt", "historical-2.txt"
    }


@pytest.mark.asyncio
async def test_snapshot_uses_relative_paths_and_full_content_checksums():
    collector_type, _, entry_type = _api()
    context = _context()
    path = str(context.output_dir / "nested" / "report.txt")
    collector = collector_type(RecordingOutputSource([entry_type(path, 4, False)], {path: b"data"}))
    assert await collector.snapshot() == {
        "nested/report.txt": hashlib.sha256(b"data").hexdigest()
    }


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


@pytest.mark.asyncio
async def test_remote_source_scans_and_reads_only_the_server_supplied_output_root():
    module = importlib.import_module(
        "agent_runtime.conversation.output_artifact_collector"
    )
    context = _context()
    output_path = str(context.output_dir / "result.txt")
    shell = RecordingShell(json.dumps([{
        "path": output_path,
        "size": 4,
        "is_symlink": False,
        "is_regular_file": True,
    }]))
    fs = RecordingRemoteFs()
    source = module.RemoteSandboxOutputSource(
        RecordingRemoteOperation(shell, fs)
    )

    entries = await source.scan(str(context.output_dir))
    content = await source.read_bytes(output_path)

    assert entries == [module.SandboxOutputEntry(
        path=output_path,
        size=4,
        is_symlink=False,
        is_regular_file=True,
    )]
    assert shell.calls[0][1] == {
        "cwd": "/",
        "environment": {"OJW_OUTPUT_ROOT": str(context.output_dir)},
    }
    assert "python3 -c" in shell.calls[0][0]
    assert fs.calls == [(output_path, {"mode": "bytes"})]
    assert content == b"data"


@pytest.mark.asyncio
@pytest.mark.parametrize("path", ["../other", "/etc", "/workspace/other/output", "/workspace/../etc"])
async def test_remote_source_rejects_non_execution_roots_before_shell(path):
    module = importlib.import_module("agent_runtime.conversation.output_artifact_collector")
    shell = RecordingShell()
    source = module.RemoteSandboxOutputSource(RecordingRemoteOperation(shell, RecordingRemoteFs()))
    with pytest.raises(module.OutputArtifactCollectionError, match="boundary"):
        await source.scan(path)
    assert shell.calls == []


@pytest.mark.asyncio
@pytest.mark.parametrize("boundary", ["scan", "read"])
async def test_remote_source_rejects_aio_nonzero_results_without_is_ok(boundary):
    module = importlib.import_module(
        "agent_runtime.conversation.output_artifact_collector"
    )
    context = _context()
    output_path = str(context.output_dir / "result.txt")
    shell = RecordingShell()
    fs = RecordingRemoteFs()
    failed_result = SimpleNamespace(code=199003, message="remote failure", data=None)
    if boundary == "scan":
        shell.result = failed_result
    else:
        fs.result = failed_result
    source = module.RemoteSandboxOutputSource(RecordingRemoteOperation(shell, fs))

    with pytest.raises(
        module.OutputArtifactCollectionError,
        match=f"output {boundary} failed",
    ):
        if boundary == "scan":
            await source.scan(str(context.output_dir))
        else:
            await source.read_bytes(output_path)
