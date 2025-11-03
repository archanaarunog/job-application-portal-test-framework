
# Basic Git Commands — common workflows you'll use

This is a compact cheat-sheet for your daily Git operations (branching, committing, pushing, and PRs).

## Branch naming guidance
- Use feature/ for new features (e.g. `feature/login-pom`)
- Use fix/ for bug fixes (e.g. `fix/login-error-handling`)
- Use chore/ for infra/docs changes (e.g. `chore/add-allure-config`)
- Keep names short, kebab-case, and include JIRA/ID if available: `feature/JIRA-123-login`.

## Common local workflows

1) Create a new feature branch from `main` (recommended):

```bash
# update local main
git checkout main
git pull origin main

# create and switch to a new branch
git checkout -b feature/login-pom
```

2) Work, stage, commit (atomic commits, clear messages):

```bash
# see changed files
git status

# add files (use '.' carefully)
git add src/test/java/com/archana/meta/pages/LoginPage.java

# commit with a concise message
git commit -m "feat(login): add LoginPage POM with enterEmail/enterPassword"
```

3) Push branch to remote for the first time:

```bash
git push -u origin feature/login-pom
```

4) Update an existing branch (after pulling remote changes):

```bash
# fetch latest
git fetch origin

# rebase or merge latest main into your branch (rebase recommended for linear history)
git checkout feature/login-pom
git rebase origin/main

# resolve conflicts if any, then continue rebase
git add <conflicted-files>
git rebase --continue

# push rebased branch (force-with-lease is safer)
git push --force-with-lease
```

5) Merge back via Pull Request

- Push your branch and open a PR on GitHub from `feature/...` into `main`.
- Add reviewers, link relevant issue, and include a short description and test instructions.

## Quick fixes / small workflows

- Amend last commit (use when you forgot to include files or fix message):

```bash
git add <file>
git commit --amend --no-edit    # edits previous commit contents without changing message
git push --force-with-lease
```

- Create a branch from a specific commit / tag:

```bash
git checkout -b fix/some-issue <commit-hash>
```

## Inspecting and cleaning up

- Show branch list (local and remote):

```bash
git branch        # local
git branch -r     # remote
git branch -a     # all
```

- Delete a local branch:

```bash
git branch -d feature/old-branch
```

- Delete a remote branch:

```bash
git push origin --delete feature/old-branch
```

## Recommended practices

- Commit often but keep commits focused and atomic.
- Write commit messages with a short header and optional body (use Conventional Commits style if you want).
- Rebase interactively to keep history clean for feature branches before opening PRs.
- Use `--force-with-lease` instead of `--force` when pushing rebased branches.

If you want, I can create a GitHub Actions workflow to automatically run tests on PRs and enforce branch protection rules. Let me know and I will add it to the plan.
