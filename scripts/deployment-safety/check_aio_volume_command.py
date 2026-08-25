#!/usr/bin/env python3
"""静态检查 AIO 更新命令，防止删除命名 Docker Volume。"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from typing import Iterable


COMMAND_SEPARATORS = re.compile(r"(?:\r?\n|&&|\|\||;|\|)")
VOLUME_OPTIONS = {"-v", "--volumes"}


def command_segments(command_text: str) -> Iterable[list[str]]:
    """将多行或链式命令拆为待静态检查的命令片段。"""
    for segment in COMMAND_SEPARATORS.split(command_text):
        stripped = segment.strip()
        if not stripped or stripped.startswith("#"):
            continue
        yield [token.lower() for token in re.findall(r"[^\s]+", stripped)]


def dangerous_operation(tokens: list[str]) -> str | None:
    """返回命中的危险 Docker 操作；未命中时返回 ``None``。"""
    for index, token in enumerate(tokens):
        arguments = tokens[index + 1 :]

        if token == "docker-compose":
            if "down" in arguments and VOLUME_OPTIONS.intersection(arguments):
                return "docker-compose down -v/--volumes"
            continue

        if token != "docker":
            continue

        if "compose" in arguments and "down" in arguments:
            if VOLUME_OPTIONS.intersection(arguments):
                return "docker compose down -v/--volumes"
        if "volume" in arguments and "rm" in arguments:
            return "docker volume rm"
        if "volume" in arguments and "prune" in arguments:
            return "docker volume prune"
        if (
            "system" in arguments
            and "prune" in arguments
            and "--volumes" in arguments
        ):
            return "docker system prune --volumes"

    return None


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="静态检查 AIO 更新命令是否会删除命名 Volume。"
    )
    source = parser.add_mutually_exclusive_group()
    source.add_argument("--command", help="需要检查的命令字符串")
    source.add_argument("--file", type=Path, help="需要检查的命令或脚本文件")
    arguments = parser.parse_args()
    if arguments.command is None and arguments.file is None:
        parser.error("必须提供 --command 或 --file。")
    return arguments


def load_command_text(arguments: argparse.Namespace) -> str | None:
    if arguments.command is not None:
        if arguments.command.strip():
            return arguments.command
        print("必须提供非空的命令字符串。", file=sys.stderr)
        return None

    try:
        return arguments.file.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as error:
        print(f"无法读取输入文件：{arguments.file}（{error}）", file=sys.stderr)
        return None


def main() -> int:
    arguments = parse_arguments()
    command_text = load_command_text(arguments)
    if command_text is None:
        return 2

    for tokens in command_segments(command_text):
        operation = dangerous_operation(tokens)
        if operation:
            print(
                f"危险命令：检测到 {operation}；该操作可能删除命名 Volume。"
                "检查器仅做静态检查，不会执行输入命令。",
                file=sys.stderr,
            )
            return 1

    print("检查通过：未发现删除命名 Volume 的危险命令（本工具不会执行输入命令）。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
