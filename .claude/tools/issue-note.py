#!/usr/bin/env python3
"""Comment on a ticket, optionally close it and/or add labels; re-reads to verify.

Usage:
  issue-note.py <iid> --body-file note.md [--close] [--label In\\ Work,Backend]
"""
from __future__ import annotations

import argparse

from _glab import fail, glab, glab_json, glab_post_json, parse_labels, project_labels, read_utf8_file


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("iid")
    ap.add_argument("--body-file", required=True)
    ap.add_argument("--close", action="store_true")
    ap.add_argument("--label", action="append")
    args = ap.parse_args()

    labels = parse_labels(args.label)
    unknown = [l for l in labels if l not in set(project_labels())]
    if unknown:
        fail(f"labels not in project: {', '.join(unknown)}")

    glab_post_json(f"projects/:id/issues/{args.iid}/notes", {"body": read_utf8_file(args.body_file)})
    if args.close or labels:
        fields = []
        if args.close:
            fields += ["-f", "state_event=close"]
        if labels:
            fields += ["-f", f"add_labels={','.join(labels)}"]
        glab("api", f"projects/:id/issues/{args.iid}", "--method", "PUT", *fields)
        issue = glab_json("api", f"projects/:id/issues/{args.iid}")
        if args.close and issue["state"] != "closed":
            fail(f"#{args.iid} still {issue['state']} after close")
        missing = [l for l in labels if l not in issue["labels"]]
        if missing:
            fail(f"labels not applied to #{args.iid}: {', '.join(missing)}")
    print(f"noted #{args.iid}" + (" and closed" if args.close else ""))


if __name__ == "__main__":
    main()
