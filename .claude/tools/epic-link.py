#!/usr/bin/env python3
"""Link child tickets to an epic via relates_to (no native hierarchy on this GitLab tier).

Usage:
  epic-link.py <epic_iid> <child_iid> [<child_iid> ...]
"""
from __future__ import annotations

import sys

from _glab import glab_post_json, project_id


def link(pid: int, epic_iid: str, child_iid: str) -> None:
    created = glab_post_json(
        f"projects/:id/issues/{epic_iid}/links",
        {"target_project_id": pid, "target_issue_iid": child_iid, "link_type": "relates_to"},
    )
    if not isinstance(created, dict) or not created.get("link_type"):
        raise RuntimeError(f"#{epic_iid} <-> #{child_iid} not linked — GitLab returned: {created!r}")
    print(f"linked #{epic_iid} <-> #{child_iid}")


def main() -> int:
    if len(sys.argv) < 3:
        print(__doc__)
        return 2
    epic_iid, children = sys.argv[1], sys.argv[2:]
    pid = project_id()
    failures = 0
    for child_iid in children:
        try:
            link(pid, epic_iid, child_iid)
        except RuntimeError as e:
            failures += 1
            print(f"[epic-link] {e}", file=sys.stderr)
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
