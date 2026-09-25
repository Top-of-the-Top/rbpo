#!/usr/bin/env python3
"""Create a Vedu GitLab issue: UTF-8 body from file, one label per axis, assignee mandatory.

Usage:
  issue-create.py --title "T" --body-file body.md \\
      --label Backlog,Medium,Backend,Feature [--assignee <gitlab-username>] [--dry-run]

Checks before anything is created (GitLab would happily accept all of these):
  - every label exists in the project (GitLab auto-creates unknown labels on issue create),
  - exactly one label per axis (state / severity / area / type),
  - the assignee resolves to a real GitLab user (default: the authenticated glab user).
After creation the assignee and labels are re-read from the response; any mismatch exits 1.
The assignee is mandatory: the Telegram notification is triggered by the assignee.
"""
from __future__ import annotations

import argparse

from _glab import (
    ISSUES_ENDPOINT, fail, glab_post_json, my_username, parse_labels,
    project_labels, read_utf8_file, user_id,
)

# Axes of the Vedu project labels (verified against the project 2026-09-21).
AXES = {
    "state": {"Plans", "Backlog", "In Work"},
    "severity": {"Major", "Medium", "Low"},
    "area": {"Backend", "Deploy", "Docs"},
    "type": {"Bug", "Feature", "Analysis", "Epic"},
}


def check_labels(labels: list[str]) -> None:
    existing = set(project_labels())
    unknown = [l for l in labels if l not in existing]
    if unknown:
        fail(f"labels not in project (GitLab would auto-create them): {', '.join(unknown)}")
    for axis, values in AXES.items():
        picked = [l for l in labels if l in values]
        if len(picked) != 1:
            fail(f"need exactly one {axis} label from {sorted(values)}, got {picked or 'none'}")
    extra = [l for l in labels if not any(l in v for v in AXES.values())]
    if extra:
        fail(f"labels outside the four axes: {', '.join(extra)}")


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--title", required=True)
    p.add_argument("--body-file", required=True)
    p.add_argument("--label", action="append", required=True)
    p.add_argument("--assignee", default=None, help="GitLab username; default: glab-authenticated user")
    p.add_argument("--dry-run", action="store_true", help="validate and print the plan, create nothing")
    args = p.parse_args()

    body = read_utf8_file(args.body_file)
    labels = parse_labels(args.label)
    assignee = args.assignee or my_username()
    uid = user_id(assignee)
    if uid is None:
        fail(f"assignee {assignee!r} not found in GitLab (pass the username, not the display name)")
    check_labels(labels)

    if args.dry_run:
        print(f"[dry-run] OK\n  title: {args.title}\n  assignee: {assignee}\n  labels: {', '.join(labels)}")
        return

    created = glab_post_json(ISSUES_ENDPOINT, {
        "title": args.title, "description": body,
        "labels": ",".join(labels), "assignee_ids": [uid],
    })
    if not isinstance(created, dict) or not created.get("iid"):
        fail(f"issue was not created, GitLab returned: {created!r}")

    print(f"iid: {created['iid']}\n{created.get('web_url', '')}")
    applied = {l.casefold() for l in created.get("labels") or []}
    dropped = [l for l in labels if l.casefold() not in applied]
    got = [a["username"] for a in created.get("assignees") or []]
    if dropped:
        fail(f"GitLab did not apply labels: {', '.join(dropped)}")
    if assignee not in got:
        fail(f"GitLab did not set assignee {assignee!r} (got {got})")


if __name__ == "__main__":
    main()
