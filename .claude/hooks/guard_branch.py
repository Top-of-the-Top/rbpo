"""Refuse edits while HEAD is main/master (CLAUDE.md §6). Fail-open on any internal error."""

from __future__ import annotations

import json
import os
import subprocess
import sys
from pathlib import Path

PROTECTED = {"main", "master"}


def main() -> int:
    try:
        payload = json.load(sys.stdin)
        target = (payload.get("tool_input") or {}).get("file_path") or ""
        root = os.environ.get("CLAUDE_PROJECT_DIR") or os.getcwd()
        if target and Path(target).resolve().parts[: len(Path(root).resolve().parts)] != Path(root).resolve().parts:
            return 0
        branch = subprocess.run(
            ["git", "symbolic-ref", "--short", "HEAD"], cwd=root,
            capture_output=True, text=True, timeout=10,
        ).stdout.strip()
    except Exception:
        return 0
    if branch in PROTECTED:
        print(f"Blocked: HEAD is '{branch}'. Create a branch first (CLAUDE.md §6).", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
