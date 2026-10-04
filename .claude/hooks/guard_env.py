"""Refuse tool calls that read or write real .env files (CLAUDE.md §11: never touch secrets).

`.env.example` is allowed. Fails open on malformed input so a broken hook never blocks work.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import PurePath

SECRET_ENV_NAME = re.compile(r"^\.env(\.(?!example$)[\w.-]+)?$")
SECRET_ENV_IN_COMMAND = re.compile(r"(?:^|[\s/\"'=<>])\.env(?:\.(?!example(?:[\s\"']|$))[\w.-]+)?(?=[\s\"';|&)]|$)")


def touches_env_file(path: str) -> bool:
    return bool(SECRET_ENV_NAME.match(PurePath(path).name))


def touches_env_command(command: str) -> bool:
    return bool(SECRET_ENV_IN_COMMAND.search(command))


def main() -> int:
    try:
        tool_input = json.load(sys.stdin).get("tool_input") or {}
    except Exception:
        return 0
    path = tool_input.get("file_path") or tool_input.get("notebook_path") or ""
    command = tool_input.get("command") or ""
    if (path and touches_env_file(path)) or (command and touches_env_command(command)):
        print(
            "Blocked: real .env files hold secrets and are off-limits to the agent (CLAUDE.md §11). "
            "Edit .env.example for new keys; ask the user to run a command with `! <cmd>` if .env is needed.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
