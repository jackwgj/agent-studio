"""Security regression tests for the conversation skill artifact cache."""

import asyncio
import io
import os
import stat
import zipfile
from unittest.mock import AsyncMock

import pytest

import agent_runtime.supervisor.skill_artifact_cache as skill_artifact_cache_module
from agent_runtime.supervisor.skill_artifact_cache import (
    SkillArtifactCache,
    SkillArtifactError,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor


def descriptor(skill_id: str, version_id: str, name: str, object_key: str) -> SkillDescriptor:
    return SkillDescriptor(
        skill_id=skill_id,
        version_id=version_id,
        name=name,
        description=f"description-{name}",
        object_key=object_key,
    )


def zip_with(member: str, data: bytes, *, compression: int = zipfile.ZIP_DEFLATED) -> bytes:
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", compression) as archive:
        archive.writestr(member, data)
    return output.getvalue()


def skill_zip(name: str, body: str) -> bytes:
    markdown = f"---\nname: {name}\ndescription: test skill\n---\n\n{body}"
    return zip_with(f"{name}/SKILL.md", markdown.encode("utf-8"))


@pytest.mark.asyncio
async def test_same_skill_version_downloads_once(tmp_path):
    downloader = AsyncMock(return_value=skill_zip("meeting-minutes", "正文标记"))
    cache = SkillArtifactCache(tmp_path, downloader=downloader)
    skill = descriptor("s1", "v1", "meeting-minutes", "u1/skills/s1/v1/a.zip")

    first = await cache.load_instructions(skill)
    second = await cache.load_instructions(skill)

    assert first == second
    assert first.endswith("正文标记")
    downloader.assert_awaited_once_with(skill.object_key)


@pytest.mark.asyncio
async def test_concurrent_same_skill_version_downloads_once(tmp_path):
    downloader = AsyncMock(return_value=skill_zip("meeting-minutes", "正文标记"))
    cache = SkillArtifactCache(tmp_path, downloader=downloader)
    skill = descriptor("s1", "v1", "meeting-minutes", "u1/skills/s1/v1/a.zip")

    loaded = await asyncio.gather(*[cache.load_instructions(skill) for _ in range(8)])

    assert len(set(loaded)) == 1
    assert loaded[0].endswith("正文标记")
    downloader.assert_awaited_once_with(skill.object_key)


@pytest.mark.asyncio
async def test_new_version_uses_new_cache_entry(tmp_path):
    downloader = AsyncMock(side_effect=[skill_zip("x", "v1"), skill_zip("x", "v2")])
    cache = SkillArtifactCache(tmp_path, downloader=downloader)

    first = await cache.load_instructions(
        descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip")
    )
    second = await cache.load_instructions(
        descriptor("s1", "v2", "x", "u/skills/s1/v2/a.zip")
    )
    assert first != second
    assert len(list(tmp_path.iterdir())) == 2


@pytest.mark.asyncio
@pytest.mark.parametrize("member", ["../escape", "/absolute", "C:/escape"])
async def test_path_traversal_is_rejected(tmp_path, member):
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=zip_with(member, b"x")))

    with pytest.raises(SkillArtifactError, match="unsafe zip path"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_object_key_outside_skill_version(tmp_path):
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock())
    skill = descriptor("s1", "v1", "x", "u/skills/s1/v2/a.zip")

    with pytest.raises(SkillArtifactError, match="unsafe object key"):
        await cache.load_instructions(skill)


@pytest.mark.asyncio
async def test_rejects_nested_zip(tmp_path):
    nested = zip_with("inner.zip", skill_zip("inner", "x"))
    payload = io.BytesIO()
    with zipfile.ZipFile(payload, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"body")
        archive.writestr("x/inner.zip", nested)
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload.getvalue()))

    with pytest.raises(SkillArtifactError, match="nested zip"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_nested_zip_without_zip_suffix(tmp_path):
    payload = io.BytesIO()
    with zipfile.ZipFile(payload, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"body")
        archive.writestr("x/embedded-data", skill_zip("inner", "x"))
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload.getvalue()))

    with pytest.raises(SkillArtifactError, match="nested zip"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_self_extracting_nested_zip_without_zip_suffix(tmp_path):
    payload = io.BytesIO()
    with zipfile.ZipFile(payload, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"body")
        archive.writestr("x/embedded-data", b"SFX-prefix" + skill_zip("inner", "x"))
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload.getvalue()))

    with pytest.raises(SkillArtifactError, match="nested zip"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_symbolic_link(tmp_path):
    payload = io.BytesIO()
    with zipfile.ZipFile(payload, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"body")
        link = zipfile.ZipInfo("x/link")
        link.create_system = 3
        link.external_attr = (stat.S_IFLNK | 0o777) << 16
        archive.writestr(link, "target")
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload.getvalue()))

    with pytest.raises(SkillArtifactError, match="unsafe zip member"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_more_than_500_entries(tmp_path):
    payload = io.BytesIO()
    with zipfile.ZipFile(payload, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"body")
        for index in range(500):
            archive.writestr(f"x/{index}.txt", b"x")
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload.getvalue()))

    with pytest.raises(SkillArtifactError, match="too many zip entries"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_archive_larger_than_10_mib(tmp_path):
    payload = zip_with("x/SKILL.md", os.urandom(10 * 1024 * 1024 + 1), compression=zipfile.ZIP_STORED)
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload))

    with pytest.raises(SkillArtifactError, match="archive too large"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_uncompressed_content_larger_than_100_mib(tmp_path):
    payload = zip_with("x/SKILL.md", b"x" * (100 * 1024 * 1024 + 1))
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=payload))

    with pytest.raises(SkillArtifactError, match="uncompressed content too large"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_path_deeper_than_10_levels(tmp_path):
    member = "x/" + "/".join(["nested"] * 10) + "/SKILL.md"
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=zip_with(member, b"body")))

    with pytest.raises(SkillArtifactError, match="zip path too deep"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_requires_a_root_skill_markdown(tmp_path):
    cache = SkillArtifactCache(
        tmp_path, downloader=AsyncMock(return_value=zip_with("x/readme.md", b"body"))
    )

    with pytest.raises(SkillArtifactError, match="exactly one root SKILL.md"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_multiple_root_skill_markdown_files(tmp_path):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"one")
        archive.writestr("y/SKILL.md", b"two")
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=output.getvalue()))

    with pytest.raises(SkillArtifactError, match="exactly one root SKILL.md"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_rejects_additional_nested_skill_markdown_file(tmp_path):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("x/SKILL.md", b"one")
        archive.writestr("x/nested/SKILL.md", b"two")
    cache = SkillArtifactCache(tmp_path, downloader=AsyncMock(return_value=output.getvalue()))

    with pytest.raises(SkillArtifactError, match="exactly one root SKILL.md"):
        await cache.load_instructions(descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip"))


@pytest.mark.asyncio
async def test_failed_download_does_not_publish_or_poison_cache(tmp_path):
    downloader = AsyncMock(side_effect=[zip_with("../escape", b"x"), skill_zip("x", "recovered")])
    cache = SkillArtifactCache(tmp_path, downloader=downloader)
    skill = descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip")

    with pytest.raises(SkillArtifactError, match="unsafe zip path"):
        await cache.load_instructions(skill)

    assert (await cache.load_instructions(skill)).endswith("recovered")
    assert list(tmp_path.iterdir()) == [tmp_path / skill.cache_key]
    assert downloader.await_count == 2


@pytest.mark.asyncio
async def test_competing_cache_instances_keep_published_artifact_on_replace_failure(tmp_path, monkeypatch):
    ready = asyncio.Event()
    started = 0

    async def downloader(_: str) -> bytes:
        nonlocal started
        started += 1
        if started == 2:
            ready.set()
        await ready.wait()
        return skill_zip("x", "published")

    original_replace = skill_artifact_cache_module.os.replace
    replace_calls = 0

    def replace_once(source, target):
        nonlocal replace_calls
        replace_calls += 1
        if replace_calls == 2:
            raise OSError("simulated replace failure")
        return original_replace(source, target)

    monkeypatch.setattr(skill_artifact_cache_module.os, "replace", replace_once)
    skill = descriptor("s1", "v1", "x", "u/skills/s1/v1/a.zip")
    first_cache = SkillArtifactCache(tmp_path, downloader=downloader)
    second_cache = SkillArtifactCache(tmp_path, downloader=downloader)

    first, second = await asyncio.gather(
        first_cache.load_instructions(skill), second_cache.load_instructions(skill)
    )

    assert first == second
    assert first.endswith("published")
    assert replace_calls == 1
