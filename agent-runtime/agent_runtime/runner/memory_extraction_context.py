"""Shared context for triggering memory extraction after execution."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class MemoryExtractionContext:
    """Encapsulates the inputs needed to trigger memory extraction.

    Grouping these related fields satisfies G.FNM.03 (avoid too many function
    arguments) and keeps the ``_trigger_memory_extraction`` signature stable
    when new fields are added.
    """

    ir_json: dict
    user_id: str
    conversation_id: str
    user_query: str
    assistant_response: str
    enable_memory_extract: bool = False
