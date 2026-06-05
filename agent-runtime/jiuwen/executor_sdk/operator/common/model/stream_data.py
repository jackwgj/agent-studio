"""Minimal StreamData model for local tests."""

from dataclasses import dataclass


@dataclass
class StreamData:
    code: str
    data: str
