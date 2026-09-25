"""Private glab helpers shared by the .claude/tools/*.py scripts (Vedu).

Trimmed from cstati's _glab.py: only what issue creation needs.
All API calls go through `glab api`, so auth is whatever `glab auth login` set up.
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any

# glab resolves `:id` from the git remote of the current directory.
ISSUES_ENDPOINT = "projects/:id/issues"
LABELS_ENDPOINT = "projects/:id/labels?per_page=100"


def glab(*args: str) -> str:
    # Update banner on stdout would break JSON parsing (cstati #1688).
    env = {**os.environ, "GLAB_CHECK_UPDATE": "false"}
    res = subprocess.run(
        ["glab", *args], capture_output=True, text=True,
        encoding="utf-8", errors="replace", env=env,
    )
    if res.returncode != 0:
        raise RuntimeError(f"glab {' '.join(args[:2])} failed: {(res.stderr or res.stdout).strip()}")
    return res.stdout


def glab_json(*args: str) -> Any:
    return json.loads(glab(*args))


def glab_post_json(endpoint: str, payload: dict[str, Any]) -> Any:
    """POST a JSON payload via --input file: Cyrillic-safe, arrays (assignee_ids) work."""
    with tempfile.NamedTemporaryFile("w", suffix=".json", encoding="utf-8", delete=False) as f:
        json.dump(payload, f, ensure_ascii=False)
        tmp = Path(f.name)
    try:
        return json.loads(glab("api", endpoint, "--input", str(tmp),
                               "-H", "Content-Type: application/json"))
    finally:
        tmp.unlink(missing_ok=True)


def read_utf8_file(path: str | Path) -> str:
    return Path(path).read_text(encoding="utf-8-sig")


def my_username() -> str:
    return glab_json("api", "user")["username"]


def user_id(username: str) -> int | None:
    found = glab_json("api", f"users?username={username}")
    return int(found[0]["id"]) if found else None


def project_labels() -> list[str]:
    return [l["name"] for l in glab_json("api", LABELS_ENDPOINT)]


def parse_labels(values: list[str] | None) -> list[str]:
    """Flatten repeated and/or comma-separated --label into an ordered, de-duplicated list."""
    out: list[str] = []
    for chunk in values or []:
        for label in chunk.split(","):
            label = label.strip()
            if label and label not in out:
                out.append(label)
    return out


def fail(msg: str) -> None:
    print(f"[error] {msg}", file=sys.stderr)
    sys.exit(1)
