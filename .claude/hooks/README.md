# .claude/hooks

Wired in `.claude/settings.json`; run on matching tool calls.

- `guard_branch.py` — PreToolUse on Edit/Write/NotebookEdit: refuses edits inside this repo while HEAD is `main`/`master`. Edits outside the repo (scratchpad, memory) are never blocked. Fails open on internal errors.
- `guard_env.py` — PreToolUse on Read/Edit/Write/NotebookEdit/Bash: refuses access to real `.env`/`.env.*` files (path or Bash command mention); `.env.example` allowed. Tests: `python3 .claude/hooks/tests/test_guard_env.py`.
- Inline PostToolUse reminder (settings.json) — nudges an `AI_USAGE.md` entry after substantive edits.
