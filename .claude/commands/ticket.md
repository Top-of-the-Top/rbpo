---
description: Solve a GitLab ticket end to end — read, branch, TDD, gate, MR
argument-hint: <ticket number> [more numbers]
allowed-tools: Agent, Bash, Read, Glob, Grep, Edit, Write
---

Take tickets: **$ARGUMENTS**

Follow skill `ticket-to-release`. Non-negotiable:

1. Read the ticket yourself first (`glab issue view <n>`); pass its substance, not just the number, to any subagent.
2. Parallel tickets → one subagent each with `isolation: "worktree"`. Never work two tickets in one worktree.
3. TDD: see the new test fail before writing production code.
4. Never claim a check passed that you did not run and read.
5. Stop at an open, green MR unless the user said to merge.

No ticket number given → ask, do not guess.
