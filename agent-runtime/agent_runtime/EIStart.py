"""CLI entry point for OLE server."""

import argparse

from EIStart_base import main as start_server


def main():
    parser = argparse.ArgumentParser(description="Open AgentBuilder Engine (OLE) MVP")
    parser.add_argument("--host", default=None, help="Bind host")
    parser.add_argument("--port", type=int, default=None, help="Bind port")
    parser.add_argument("--log-level", default=None, help="Log level")

    args = parser.parse_args()

    from agent_runtime.common import settings

    if args.host is not None:
        settings.server.host = args.host
    if args.port is not None:
        settings.server.port = args.port
    if args.log_level is not None:
        settings.server.log_level = args.log_level

    start_server()


if __name__ == "__main__":
    main()
