import json
import subprocess
import sys
import unittest
from pathlib import Path

HOOK = Path(__file__).resolve().parents[1] / "guard_env.py"


def run(tool, tool_input):
    res = subprocess.run(
        [sys.executable, str(HOOK)],
        input=json.dumps({"tool_name": tool, "tool_input": tool_input}),
        capture_output=True, text=True,
    )
    return res.returncode


class GuardEnvTest(unittest.TestCase):
    def test_blocks_read_of_env(self):
        self.assertEqual(2, run("Read", {"file_path": "/repo/.env"}))

    def test_blocks_edit_of_env_variant(self):
        self.assertEqual(2, run("Edit", {"file_path": "/repo/backend/.env.local"}))

    def test_allows_env_example(self):
        self.assertEqual(0, run("Edit", {"file_path": "/repo/.env.example"}))

    def test_allows_unrelated_file(self):
        self.assertEqual(0, run("Read", {"file_path": "/repo/environment.md"}))

    def test_blocks_bash_cat_env(self):
        self.assertEqual(2, run("Bash", {"command": "cat .env"}))

    def test_blocks_bash_env_in_path(self):
        self.assertEqual(2, run("Bash", {"command": "grep JWT backend/.env"}))

    def test_allows_git_add_all(self):
        self.assertEqual(0, run("Bash", {"command": "git add -A"}))

    def test_allows_bash_with_env_example_only(self):
        self.assertEqual(0, run("Bash", {"command": "diff .env.example docs/x"}))

    def test_allows_bash_without_env(self):
        self.assertEqual(0, run("Bash", {"command": "./gradlew test"}))

    def test_malformed_input_fails_open(self):
        res = subprocess.run([sys.executable, str(HOOK)], input="not json", capture_output=True, text=True)
        self.assertEqual(0, res.returncode)


if __name__ == "__main__":
    unittest.main()
