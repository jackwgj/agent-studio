"""Normalize result contracts returned by remote sandbox operations."""

from __future__ import annotations


def operation_succeeded(result: object) -> bool:
    """Return success only when a known result contract explicitly says so."""
    is_ok = getattr(result, "is_ok", None)
    if callable(is_ok):
        try:
            outer_succeeded = bool(is_ok())
        except Exception:
            return False
    elif isinstance(is_ok, bool):
        outer_succeeded = is_ok
    else:
        code = getattr(result, "code", None)
        if code is None:
            return False
        try:
            outer_succeeded = int(code) == 0
        except (TypeError, ValueError):
            return False
    if not outer_succeeded:
        return False

    # Shell/Code operations have a second success boundary. A successful HTTP/
    # SDK wrapper must not hide a failed command inside data.exit_code.
    data = getattr(result, "data", None)
    exit_code = getattr(data, "exit_code", None)
    if exit_code is None:
        return True
    try:
        return int(exit_code) == 0
    except (TypeError, ValueError):
        return False


def operation_error_detail(result: object) -> object:
    """Extract a diagnostic value without changing success semantics."""
    error = getattr(result, "error", None)
    if callable(error):
        try:
            return error()
        except Exception:
            return None
    if error is not None:
        return error
    return getattr(result, "message", None)
