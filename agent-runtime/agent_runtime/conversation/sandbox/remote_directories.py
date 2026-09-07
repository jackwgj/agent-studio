"""Build guarded POSIX directory programs executed only by the remote sandbox.

Directory descriptors and O_NOFOLLOW reject symlinks at every path component;
plain mkdir -p / realpath checks alone would follow a substituted ancestor.
This is path hardening, not a security boundary against arbitrary shared Shell.
"""

import base64
import json
import zlib


_DIRECTORY_HELPERS = r'''
import json
import os
import stat

def path_parts(path, absolute):
    if not isinstance(path, str) or not path or "\\" in path or "\x00" in path:
        raise ValueError("invalid directory path")
    if path.startswith("/") != absolute or path.startswith("//"):
        raise ValueError("invalid directory path boundary")
    parts = path.split("/")
    if any(part in (".", "..") for part in parts):
        raise ValueError("directory traversal rejected")
    return [part for part in parts if part]

def open_child(parent_fd, name, create=False):
    if create:
        try:
            os.mkdir(name, mode=0o700, dir_fd=parent_fd)
        except FileExistsError:
            pass
    return os.open(name, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW, dir_fd=parent_fd)

def open_relative(parent_fd, path, create=False):
    parts = path_parts(path, False)
    fd = os.dup(parent_fd)
    try:
        for part in parts:
            next_fd = open_child(fd, part, create)
            os.close(fd)
            fd = next_fd
        return fd
    except BaseException:
        os.close(fd)
        raise

def open_absolute(path, missing_ok=False):
    parts = path_parts(path, True)
    fd = os.open("/", os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
    try:
        for part in parts:
            next_fd = open_child(fd, part)
            os.close(fd)
            fd = next_fd
        return fd
    except FileNotFoundError:
        os.close(fd)
        if missing_ok:
            return None
        raise
    except BaseException:
        os.close(fd)
        raise
'''


def remote_directory_command(script: str, arguments: dict) -> str:
    """Encode server-derived parameters without interpolating them into Shell."""
    source = _DIRECTORY_HELPERS + "\narguments = json.loads(" + repr(json.dumps(arguments)) + ")\n" + script
    # AIO's interactive Shell can silently discard long command lines. Compress
    # our internal program and repeated paths; do not change the SDK Provider.
    encoded = base64.b64encode(zlib.compress(source.encode("utf-8"))).decode("ascii")
    return "python3 -c \"import base64,zlib;exec(zlib.decompress(base64.b64decode('" + encoded + "')))\""
