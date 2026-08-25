"""AIO Volume 更新安全检查器的端到端 CLI 测试。"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

import pytest


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
CHECKER = REPOSITORY_ROOT / "scripts" / "deployment-safety" / "check_aio_volume_command.py"


def run_checker(*arguments: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(CHECKER), *arguments],
        cwd=REPOSITORY_ROOT,
        capture_output=True,
        text=True,
        check=False,
    )


def test_allows_container_only_update_command() -> None:
    result = run_checker(
        "--command",
        "docker compose pull && docker compose up -d --force-recreate",
    )

    assert result.returncode == 0
    assert "检查通过" in result.stdout


@pytest.mark.parametrize(
    "command",
    [
        "docker compose down -v",
        "docker compose down --volumes",
        "docker-compose down -v",
        "docker-compose down --volumes",
        "docker compose -f aio-compose.yml down -v",
        "docker volume rm aio_workspace",
        "docker volume prune",
        "docker system prune --volumes",
        "docker system --volumes prune",
        "docker compose pull\ndocker compose down --volumes",
    ],
)
def test_rejects_volume_deleting_commands(command: str) -> None:
    result = run_checker("--command", command)

    assert result.returncode != 0
    assert "危险命令" in result.stderr


def test_checks_command_text_from_file(tmp_path: Path) -> None:
    command_file = tmp_path / "update.sh"
    command_file.write_text(
        "docker compose pull\ndocker compose up -d --force-recreate\n",
        encoding="utf-8",
    )

    result = run_checker("--file", str(command_file))

    assert result.returncode == 0
    assert "检查通过" in result.stdout


def test_rejects_dangerous_command_in_multiline_file(tmp_path: Path) -> None:
    command_file = tmp_path / "update.sh"
    command_file.write_text(
        "docker compose pull\ndocker volume prune\n",
        encoding="utf-8",
    )

    result = run_checker("--file", str(command_file))

    assert result.returncode != 0
    assert "危险命令" in result.stderr


def test_fails_closed_for_unreadable_or_missing_input_file() -> None:
    result = run_checker("--file", "does-not-exist.sh")

    assert result.returncode != 0
    assert "无法读取" in result.stderr


def test_fails_closed_for_non_utf8_input_file(tmp_path: Path) -> None:
    command_file = tmp_path / "invalid-encoding.sh"
    command_file.write_bytes(b"\xff\xfe")

    result = run_checker("--file", str(command_file))

    assert result.returncode != 0
    assert "无法读取" in result.stderr


def test_fails_closed_when_no_input_is_supplied() -> None:
    result = run_checker()

    assert result.returncode != 0
    assert "必须提供" in result.stderr


def test_performs_static_check_without_executing_safe_input() -> None:
    result = run_checker("--command", "aio-update-command-that-must-not-run")

    assert result.returncode == 0
    assert "检查通过" in result.stdout
