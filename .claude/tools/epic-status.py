#!/usr/bin/env python3
"""Report an epic's linked (relates_to) children and whether the epic is closeable.

Closure condition: every linked child is closed. Reads real issue state, not the
epic body's checklist, which drifts.

Usage:
  epic-status.py <epic_iid>
"""
from __future__ import annotations

import sys

from _glab import glab_json


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    epic_iid = sys.argv[1]
    links = glab_json("api", f"projects/:id/issues/{epic_iid}/links")
    if not links:
        print(f"#{epic_iid} has no relates_to links — nothing to check")
        return 0
    print(f"Epic #{epic_iid} — {len(links)} linked children:\n")
    for l in links:
        mark = " " if l["state"] == "opened" else "x"
        print(f"  [{mark}] #{l['iid']} ({l['state']}) — {l['title']}")
    still_open = [l for l in links if l["state"] == "opened"]
    print()
    if still_open:
        print(f"NOT closeable — open: {', '.join('#' + str(l['iid']) for l in still_open)}")
        return 1
    print(f"Closeable — all {len(links)} linked children are closed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
