#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "${SCRIPT_DIR}")"

TARGET_BRANCH=""
BASE_BRANCH="main"
FETCH=1
PULL=0
INCLUDE_ROOT=0
DRY_RUN=0

usage() {
  cat <<'USAGE'
Usage:
  scripts/sync-module-branches.sh [branch] [options]

Synchronize all git submodules to the same branch.

By default, the target branch is the current branch of the parent repository.
If a submodule already has the target branch, the script switches to it.
If origin/<target> exists, the script creates/tracks the local branch from it.
Otherwise, the script creates the target branch from origin/main or local main.

Options:
  -b, --branch <name>       Target branch. Defaults to the parent repository branch.
      --base <name>         Base branch for newly created branches. Defaults to main.
      --no-fetch            Do not fetch origin before checking remote branches.
      --pull                Fast-forward pull after switching to the target branch.
      --include-root        Also switch the parent repository.
      --dry-run             Print actions without changing branches.
  -h, --help                Show this help.

Examples:
  scripts/sync-module-branches.sh
  scripts/sync-module-branches.sh feat/agp-9.2.1 --pull
  scripts/sync-module-branches.sh --branch main --include-root
USAGE
}

log() {
  printf '%s\n' "$*"
}

run() {
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    printf '  DRY-RUN:'
    printf ' %q' "$@"
    printf '\n'
  else
    "$@"
  fi
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      -b|--branch)
        TARGET_BRANCH="${2:-}"
        if [[ -z "${TARGET_BRANCH}" ]]; then
          log "Missing branch name for $1"
          exit 1
        fi
        shift 2
        ;;
      --base)
        BASE_BRANCH="${2:-}"
        if [[ -z "${BASE_BRANCH}" ]]; then
          log "Missing branch name for --base"
          exit 1
        fi
        shift 2
        ;;
      --no-fetch)
        FETCH=0
        shift
        ;;
      --pull)
        PULL=1
        shift
        ;;
      --include-root)
        INCLUDE_ROOT=1
        shift
        ;;
      --dry-run)
        DRY_RUN=1
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      -*)
        log "Unknown option: $1"
        usage
        exit 1
        ;;
      *)
        if [[ -n "${TARGET_BRANCH}" ]]; then
          log "Unexpected argument: $1"
          usage
          exit 1
        fi
        TARGET_BRANCH="$1"
        shift
        ;;
    esac
  done
}

current_parent_branch() {
  git -C "${ROOT_DIR}" symbolic-ref --quiet --short HEAD
}

ensure_clean_worktree() {
  local repo_dir="$1"
  local label="$2"

  if [[ -n "$(git -C "${repo_dir}" status --porcelain)" ]]; then
    log "Refusing to switch ${label}: worktree has uncommitted changes."
    git -C "${repo_dir}" status --short
    exit 1
  fi
}

has_local_branch() {
  local repo_dir="$1"
  local branch="$2"
  git -C "${repo_dir}" show-ref --verify --quiet "refs/heads/${branch}"
}

has_remote_branch() {
  local repo_dir="$1"
  local branch="$2"
  git -C "${repo_dir}" show-ref --verify --quiet "refs/remotes/origin/${branch}"
}

sync_repo_branch() {
  local repo_dir="$1"
  local label="$2"

  log "==> ${label}"
  ensure_clean_worktree "${repo_dir}" "${label}"

  if [[ "${FETCH}" -eq 1 ]]; then
    run git -C "${repo_dir}" fetch --prune origin
  fi

  if has_local_branch "${repo_dir}" "${TARGET_BRANCH}"; then
    run git -C "${repo_dir}" switch "${TARGET_BRANCH}"
  elif has_remote_branch "${repo_dir}" "${TARGET_BRANCH}"; then
    run git -C "${repo_dir}" switch --track -c "${TARGET_BRANCH}" "origin/${TARGET_BRANCH}"
  elif has_remote_branch "${repo_dir}" "${BASE_BRANCH}"; then
    run git -C "${repo_dir}" switch -c "${TARGET_BRANCH}" "origin/${BASE_BRANCH}"
  elif has_local_branch "${repo_dir}" "${BASE_BRANCH}"; then
    run git -C "${repo_dir}" switch -c "${TARGET_BRANCH}" "${BASE_BRANCH}"
  else
    log "Cannot create ${TARGET_BRANCH} in ${label}: base branch '${BASE_BRANCH}' was not found."
    exit 1
  fi

  if [[ "${PULL}" -eq 1 ]]; then
    run git -C "${repo_dir}" pull --ff-only
  fi

  if [[ "${DRY_RUN}" -eq 1 ]]; then
    log "  target branch: ${TARGET_BRANCH}"
  else
    log "  current branch: $(git -C "${repo_dir}" branch --show-current)"
  fi
}

submodule_paths() {
  git -C "${ROOT_DIR}" config --file .gitmodules --get-regexp '^submodule\..*\.path$' |
    awk '{print $2}'
}

main() {
  parse_args "$@"

  if [[ -z "${TARGET_BRANCH}" ]]; then
    TARGET_BRANCH="$(current_parent_branch)"
  fi

  if [[ -z "${TARGET_BRANCH}" ]]; then
    log "Could not determine target branch. Pass one with --branch."
    exit 1
  fi

  log "Target branch: ${TARGET_BRANCH}"
  log "Base branch: ${BASE_BRANCH}"

  if [[ "${INCLUDE_ROOT}" -eq 1 ]]; then
    sync_repo_branch "${ROOT_DIR}" "$(basename "${ROOT_DIR}")"
  fi

  while IFS= read -r path; do
    [[ -z "${path}" ]] && continue
    sync_repo_branch "${ROOT_DIR}/${path}" "${path}"
  done < <(submodule_paths)
}

main "$@"

