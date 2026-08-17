"""Safe, version-isolated cache for conversation skill ZIP artifacts."""

import asyncio
from collections.abc import Awaitable, Callable
import io
import os
from pathlib import Path, PurePosixPath, PureWindowsPath
import re
import shutil
import stat
import tempfile
import threading
import time
import zipfile

from agent_runtime.common.config import settings
from storage import get_storage_provider

from .skill_model import SkillDescriptor


MAX_ARCHIVE_BYTES = 10 * 1024 * 1024
MAX_UNCOMPRESSED_BYTES = 100 * 1024 * 1024
MAX_FILE_BYTES = 100 * 1024 * 1024
MAX_ZIP_ENTRIES = 500
MAX_PATH_DEPTH = 10
MAX_COMPRESSION_RATIO = 100
_FORBIDDEN_ZIP_FLAGS = 0x01 | 0x20 | 0x40
_WINDOWS_RESERVED_NAMES = {
    "CON", "PRN", "AUX", "NUL", "CLOCK$", "CONIN$", "CONOUT$",
    *(f"COM{number}" for number in range(1, 10)),
    *(f"LPT{number}" for number in range(1, 10)),
}
_WORKER_LIMIT = threading.BoundedSemaphore(2)
_process_locks: dict[str, tuple[threading.Lock, int]] = {}
_process_locks_guard = threading.Lock()


class SkillArtifactError(ValueError):
    """Raised when a skill artifact cannot safely enter the local cache."""


Downloader = Callable[[str], Awaitable[bytes]]


async def _download(object_key: str) -> bytes:
    provider = get_storage_provider()
    return await provider.get_object_bytes(object_key)


class _CacheFileLock:
    """A cache-key lock shared by cache instances and operating-system processes."""

    def __init__(self, root: Path, cache_key: str) -> None:
        self._root = root
        self._cache_key = cache_key
        self._local_lock: threading.Lock | None = None
        self._file = None
        self._locked = False

    def acquire(self) -> None:
        with _process_locks_guard:
            lock, references = _process_locks.get(self._cache_key, (threading.Lock(), 0))
            _process_locks[self._cache_key] = (lock, references + 1)
        self._local_lock = lock
        try:
            lock.acquire()
            lock_path = self._root.parent / f".{self._root.name}.{self._cache_key}.lock"
            lock_path.parent.mkdir(parents=True, exist_ok=True)
            self._file = lock_path.open("a+b")
            self._file.seek(0, os.SEEK_END)
            if self._file.tell() == 0:
                self._file.write(b"0")
                self._file.flush()
            self._file.seek(0)
            self._lock_file()
            self._locked = True
        except Exception:
            self.release()
            raise

    def release(self) -> None:
        try:
            if self._file is not None and self._locked:
                self._unlock_file()
            if self._file is not None:
                self._file.close()
        finally:
            self._file = None
            self._locked = False
            if self._local_lock is not None:
                self._local_lock.release()
                with _process_locks_guard:
                    lock, references = _process_locks[self._cache_key]
                    if references == 1:
                        del _process_locks[self._cache_key]
                    else:
                        _process_locks[self._cache_key] = (lock, references - 1)
                self._local_lock = None

    def _lock_file(self) -> None:
        if os.name == "nt":
            import msvcrt

            while True:
                try:
                    msvcrt.locking(self._file.fileno(), msvcrt.LK_NBLCK, 1)
                    return
                except PermissionError:
                    time.sleep(0.05)
        else:
            import fcntl

            fcntl.flock(self._file.fileno(), fcntl.LOCK_EX)

    def _unlock_file(self) -> None:
        if os.name == "nt":
            import msvcrt

            self._file.seek(0)
            msvcrt.locking(self._file.fileno(), msvcrt.LK_UNLCK, 1)
        else:
            import fcntl

            fcntl.flock(self._file.fileno(), fcntl.LOCK_UN)


class SkillArtifactCache:
    """Download, validate, and atomically cache one skill version at a time."""

    def __init__(self, root: Path, *, downloader: Downloader) -> None:
        self._root = Path(root)
        self._downloader = downloader

    async def load_instructions(self, skill: SkillDescriptor) -> str:
        """Return the sole root ``SKILL.md`` after safely caching ``skill``."""
        self._validate_object_key(skill)
        cache_dir = self._root / skill.cache_key
        cached = await self._run_blocking(self._read_cached_instructions, cache_dir)
        if cached is not None:
            return cached

        lock = _CacheFileLock(self._root, skill.cache_key)
        await _acquire_lock(lock)
        try:
            cached = await self._run_blocking(self._read_cached_instructions, cache_dir)
            if cached is not None:
                return cached
            artifact = await self._downloader(skill.object_key)
            if not isinstance(artifact, bytes) or len(artifact) > MAX_ARCHIVE_BYTES:
                raise SkillArtifactError("archive too large")
            return await self._run_blocking(self._stage_and_publish, artifact, cache_dir)
        finally:
            await _release_lock(lock)

    @staticmethod
    async def _run_blocking(function, *args):
        return await asyncio.to_thread(_run_limited, function, *args)

    @staticmethod
    def _validate_object_key(skill: SkillDescriptor) -> None:
        key = skill.object_key
        if not key or "\\" in key:
            raise SkillArtifactError("unsafe object key")
        parts = _raw_posix_parts(key, allow_directory=False, local_path=False)
        if PureWindowsPath(key).is_absolute() or re.match(r"^[A-Za-z]:", key):
            raise SkillArtifactError("unsafe object key")
        expected = ("skills", skill.skill_id, skill.version_id)
        if not all(expected) or any("/" in part or "\\" in part for part in expected):
            raise SkillArtifactError("unsafe object key")
        if not any(tuple(parts[index : index + 3]) == expected for index in range(len(parts) - 2)):
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

    def _stage_and_publish(self, artifact: bytes, cache_dir: Path) -> str:
        self._root.mkdir(parents=True, exist_ok=True)
        temp_dir = Path(tempfile.mkdtemp(prefix=f".{cache_dir.name}.", dir=self._root))
        staged_dir = temp_dir / "artifact"
        try:
            instructions = self._extract_and_validate(artifact, staged_dir)
            cached = self._read_cached_instructions(cache_dir)
            if cached is not None:
                return cached
            if cache_dir.exists() or cache_dir.is_symlink():
                raise SkillArtifactError("invalid existing cache entry")
            try:
                os.replace(staged_dir, cache_dir)
            except OSError as error:
                cached = self._read_cached_instructions(cache_dir)
                if cached is not None:
                    return cached
                raise SkillArtifactError("cache publish failed") from error
            return instructions
        except SkillArtifactError:
            raise
        except (OSError, EOFError, RuntimeError, NotImplementedError, zipfile.BadZipFile) as error:
            raise SkillArtifactError("invalid skill archive") from error
        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)

    def _extract_and_validate(self, artifact: bytes, staged_dir: Path) -> str:
        try:
            archive = zipfile.ZipFile(io.BytesIO(artifact))
        except (OSError, RuntimeError, NotImplementedError, zipfile.BadZipFile) as error:
            raise SkillArtifactError("invalid skill archive") from error

        with archive:
            members = archive.infolist()
            if len(members) > MAX_ZIP_ENTRIES:
                raise SkillArtifactError("too many zip entries")
            validated_members = [(member, self._validate_member(member)) for member in members]
            skill_members = [
                member for member, path in validated_members
                if not member.is_dir() and path.name == "SKILL.md" and len(path.parts) == 2
            ]
            all_skills = sum(
                not member.is_dir() and path.name == "SKILL.md" for member, path in validated_members
            )
            if len(skill_members) != 1 or all_skills != 1:
                raise SkillArtifactError("exactly one root SKILL.md is required")

            root_name: str | None = None
            declared_total = 0
            for member, path in validated_members:
                if root_name is None:
                    root_name = path.parts[0]
                elif path.parts[0] != root_name:
                    raise SkillArtifactError("unsafe zip path")
                if member.is_dir():
                    continue
                declared_total += member.file_size
                if declared_total > MAX_UNCOMPRESSED_BYTES:
                    raise SkillArtifactError("uncompressed content too large")

            staged_dir.mkdir()
            actual_total = 0
            instructions: str | None = None
            for member, path in validated_members:
                if member.is_dir():
                    continue
                target = staged_dir.joinpath(*path.parts[1:])
                target.parent.mkdir(parents=True, exist_ok=True)
                written = 0
                with archive.open(member) as source, target.open("xb") as destination:
                    while chunk := source.read(64 * 1024):
                        written += len(chunk)
                        actual_total += len(chunk)
                        if written > MAX_FILE_BYTES or actual_total > MAX_UNCOMPRESSED_BYTES:
                            raise SkillArtifactError("uncompressed content too large")
                        destination.write(chunk)
                if written != member.file_size:
                    raise SkillArtifactError("invalid skill archive")
                if zipfile.is_zipfile(target):
                    raise SkillArtifactError("nested zip")
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
        is_directory = member.is_dir()
        parts = _raw_posix_parts(name, allow_directory=is_directory, local_path=True)
        path = PurePosixPath(*parts)
        if len(path.parts) - 1 > MAX_PATH_DEPTH:
            raise SkillArtifactError("zip path too deep")
        mode = member.external_attr >> 16
        file_type = stat.S_IFMT(mode)
        if file_type not in {0, stat.S_IFREG, stat.S_IFDIR}:
            raise SkillArtifactError("unsafe zip member")
        if is_directory:
            if (file_type not in {0, stat.S_IFDIR}) or member.file_size or member.compress_size:
                raise SkillArtifactError("unsafe zip member")
        elif file_type == stat.S_IFDIR:
            raise SkillArtifactError("unsafe zip member")
        if member.compress_type not in {zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED}:
            raise SkillArtifactError("unsupported zip compression")
        if member.flag_bits & _FORBIDDEN_ZIP_FLAGS:
            raise SkillArtifactError("unsafe zip flags")
        if member.file_size < 0 or member.file_size > MAX_FILE_BYTES:
            raise SkillArtifactError("uncompressed content too large")
        if member.compress_size < 0 or member.compress_size > MAX_ARCHIVE_BYTES:
            raise SkillArtifactError("invalid skill archive")
        if member.file_size and not member.compress_size:
            raise SkillArtifactError("zip compression ratio too high")
        if member.compress_size and member.file_size > member.compress_size * MAX_COMPRESSION_RATIO:
            raise SkillArtifactError("zip compression ratio too high")
        if member.header_offset < 0:
            raise SkillArtifactError("invalid skill archive")
        return path


def _run_limited(function, *args):
    with _WORKER_LIMIT:
        return function(*args)


async def _acquire_lock(lock: _CacheFileLock) -> None:
    """Acquire in a worker and release it even if the waiting task is cancelled."""
    task = asyncio.create_task(asyncio.to_thread(lock.acquire))
    try:
        await asyncio.shield(task)
    except BaseException:
        try:
            await asyncio.shield(task)
        finally:
            await _release_lock(lock)
        raise


async def _release_lock(lock: _CacheFileLock) -> None:
    await asyncio.shield(asyncio.to_thread(lock.release))


def _raw_posix_parts(value: str, *, allow_directory: bool, local_path: bool) -> tuple[str, ...]:
    """Validate raw POSIX segments before any path library can normalize them."""
    message = "unsafe zip path" if local_path else "unsafe object key"
    if not value or "\\" in value or value.startswith("/"):
        raise SkillArtifactError(message)
    raw_value = value[:-1] if allow_directory and value.endswith("/") else value
    if allow_directory != value.endswith("/"):
        raise SkillArtifactError(message)
    parts = raw_value.split("/")
    if not raw_value or any(part in {"", ".", ".."} for part in parts):
        raise SkillArtifactError(message)
    if any(any(ord(character) < 32 or ord(character) == 127 for character in part) for part in parts):
        raise SkillArtifactError(message)
    if local_path:
        for part in parts:
            if ":" in part or _is_windows_reserved_name(part):
                raise SkillArtifactError(message)
    return tuple(parts)


def _is_windows_reserved_name(part: str) -> bool:
    return part.rstrip(". ").split(".", 1)[0].upper() in _WINDOWS_RESERVED_NAMES


_default_cache: SkillArtifactCache | None = None


def default_cache() -> SkillArtifactCache:
    """Return the process-wide cache rooted in the configured skill directory."""
    global _default_cache
    if _default_cache is None:
        root = Path(settings.skill_storage.skill_storage_dir) / "conversation-skills"
        _default_cache = SkillArtifactCache(root, downloader=_download)
    return _default_cache
