"""Safe, version-isolated cache for conversation skill ZIP artifacts."""

import asyncio
from collections.abc import Awaitable, Callable
import io
import os
from pathlib import Path, PurePosixPath, PureWindowsPath
import shutil
import stat
import tempfile
import zipfile

from agent_runtime.common.config import settings
from storage import get_storage_provider

from .skill_model import SkillDescriptor


MAX_ARCHIVE_BYTES = 10 * 1024 * 1024
MAX_UNCOMPRESSED_BYTES = 100 * 1024 * 1024
MAX_FILE_BYTES = 100 * 1024 * 1024
MAX_ZIP_ENTRIES = 500
MAX_PATH_DEPTH = 10


class SkillArtifactError(ValueError):
    """Raised when a skill artifact cannot safely enter the local cache."""


Downloader = Callable[[str], Awaitable[bytes]]


async def _download(object_key: str) -> bytes:
    provider = get_storage_provider()
    return await provider.get_object_bytes(object_key)


class SkillArtifactCache:
    """Download, validate, and atomically cache one skill version at a time."""

    def __init__(self, root: Path, *, downloader: Downloader) -> None:
        self._root = Path(root)
        self._downloader = downloader
        self._locks: dict[str, asyncio.Lock] = {}

    async def load_instructions(self, skill: SkillDescriptor) -> str:
        """Return the sole root ``SKILL.md`` after safely caching ``skill``."""
        self._validate_object_key(skill)
        cache_dir = self._root / skill.cache_key
        cached = self._read_cached_instructions(cache_dir)
        if cached is not None:
            return cached

        lock = self._locks.setdefault(skill.cache_key, asyncio.Lock())
        async with lock:
            cached = self._read_cached_instructions(cache_dir)
            if cached is not None:
                return cached
            return await self._download_and_publish(skill, cache_dir)

    @staticmethod
    def _validate_object_key(skill: SkillDescriptor) -> None:
        key = skill.object_key
        if not key or "\\" in key:
            raise SkillArtifactError("unsafe object key")
        path = PurePosixPath(key)
        windows_path = PureWindowsPath(key)
        if (
            path.is_absolute()
            or windows_path.is_absolute()
            or any(part in {"", ".", ".."} or ":" in part for part in path.parts)
        ):
            raise SkillArtifactError("unsafe object key")
        expected = ("skills", skill.skill_id, skill.version_id)
        if not all(expected) or any("/" in part or "\\" in part for part in expected):
            raise SkillArtifactError("unsafe object key")
        if not any(tuple(path.parts[index : index + 3]) == expected for index in range(len(path.parts) - 2)):
            raise SkillArtifactError("unsafe object key")

    @staticmethod
    def _read_cached_instructions(cache_dir: Path) -> str | None:
        instruction_file = cache_dir / "SKILL.md"
        if not instruction_file.is_file() or instruction_file.is_symlink():
            return None
        try:
            return instruction_file.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError) as error:
            raise SkillArtifactError("cached SKILL.md cannot be read") from error

    async def _download_and_publish(self, skill: SkillDescriptor, cache_dir: Path) -> str:
        artifact = await self._downloader(skill.object_key)
        if not isinstance(artifact, bytes) or len(artifact) > MAX_ARCHIVE_BYTES:
            raise SkillArtifactError("archive too large")

        self._root.mkdir(parents=True, exist_ok=True)
        temp_dir = Path(tempfile.mkdtemp(prefix=f".{skill.cache_key}.", dir=self._root))
        staged_dir = temp_dir / "artifact"
        try:
            instructions = self._extract_and_validate(artifact, staged_dir)
            cached = self._read_cached_instructions(cache_dir)
            if cached is not None:
                return cached
            if cache_dir.exists() or cache_dir.is_symlink():
                raise SkillArtifactError("invalid existing cache entry")
            os.replace(staged_dir, cache_dir)
            return instructions
        except (OSError, zipfile.BadZipFile) as error:
            raise SkillArtifactError("invalid skill archive") from error
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    def _extract_and_validate(self, artifact: bytes, staged_dir: Path) -> str:
        try:
            archive = zipfile.ZipFile(io.BytesIO(artifact))
        except zipfile.BadZipFile as error:
            raise SkillArtifactError("invalid skill archive") from error

        with archive:
            members = archive.infolist()
            if len(members) > MAX_ZIP_ENTRIES:
                raise SkillArtifactError("too many zip entries")

            validated_members = [(member, self._validate_member(member)) for member in members]
            all_skill_members = [
                member
                for member, relative in validated_members
                if not member.is_dir() and relative.name == "SKILL.md"
            ]
            if len(all_skill_members) != 1:
                raise SkillArtifactError("exactly one root SKILL.md is required")
            skill_members = [
                member
                for member, relative in validated_members
                if not member.is_dir() and relative.name == "SKILL.md" and len(relative.parts) == 2
            ]
            if len(skill_members) != 1:
                raise SkillArtifactError("exactly one root SKILL.md is required")

            root_name: str | None = None
            total_size = 0
            for member, relative in validated_members:
                if root_name is None:
                    root_name = relative.parts[0]
                elif relative.parts[0] != root_name:
                    raise SkillArtifactError("unsafe zip path")
                if member.is_dir():
                    continue
                total_size += member.file_size
                if member.file_size > MAX_FILE_BYTES or total_size > MAX_UNCOMPRESSED_BYTES:
                    raise SkillArtifactError("uncompressed content too large")
                if self._is_nested_zip(archive, member):
                    raise SkillArtifactError("nested zip")

            staged_dir.mkdir()
            instructions: str | None = None
            for member in members:
                if member.is_dir():
                    continue
                relative = PurePosixPath(member.filename)
                target = staged_dir.joinpath(*relative.parts[1:])
                target.parent.mkdir(parents=True, exist_ok=True)
                written = 0
                with archive.open(member) as source, target.open("xb") as destination:
                    while chunk := source.read(64 * 1024):
                        written += len(chunk)
                        if written > MAX_FILE_BYTES:
                            raise SkillArtifactError("uncompressed content too large")
                        destination.write(chunk)
                if written != member.file_size:
                    raise SkillArtifactError("invalid skill archive")
                if member is skill_members[0]:
                    try:
                        instructions = target.read_text(encoding="utf-8")
                    except UnicodeDecodeError as error:
                        raise SkillArtifactError("SKILL.md must be UTF-8") from error

            if instructions is None:
                raise SkillArtifactError("exactly one root SKILL.md is required")
            return instructions

    @staticmethod
    def _validate_member(member: zipfile.ZipInfo) -> PurePosixPath:
        name = member.filename
        path = PurePosixPath(name)
        windows_path = PureWindowsPath(name)
        if (
            not name
            or "\\" in name
            or path.is_absolute()
            or windows_path.is_absolute()
            or any(part in {"", ".", ".."} or ":" in part for part in path.parts)
        ):
            raise SkillArtifactError("unsafe zip path")
        if len(path.parts) - 1 > MAX_PATH_DEPTH:
            raise SkillArtifactError("zip path too deep")

        mode = member.external_attr >> 16
        file_type = stat.S_IFMT(mode)
        if file_type not in {0, stat.S_IFREG, stat.S_IFDIR} or stat.S_ISLNK(mode):
            raise SkillArtifactError("unsafe zip member")
        if not member.is_dir() and name.lower().endswith(".zip"):
            raise SkillArtifactError("nested zip")
        return path

    @staticmethod
    def _is_nested_zip(archive: zipfile.ZipFile, member: zipfile.ZipInfo) -> bool:
        """Reject ZIP payloads even when their member name hides the suffix."""
        with archive.open(member) as source, tempfile.SpooledTemporaryFile(
            max_size=1024 * 1024
        ) as payload:
            copied = 0
            while chunk := source.read(64 * 1024):
                copied += len(chunk)
                if copied > MAX_FILE_BYTES:
                    raise SkillArtifactError("uncompressed content too large")
                payload.write(chunk)
            payload.seek(0)
            return zipfile.is_zipfile(payload)

_default_cache: SkillArtifactCache | None = None


def default_cache() -> SkillArtifactCache:
    """Return the process-wide cache rooted in the configured skill directory."""
    global _default_cache
    if _default_cache is None:
        root = Path(settings.skill_storage.skill_storage_dir) / "conversation-skills"
        _default_cache = SkillArtifactCache(root, downloader=_download)
    return _default_cache
