#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR=$(dirname "${SCRIPT_DIR}")

FALSE=1
TRUE=0

BLACK=$'\e[30m'
RED=$'\e[31m'
GREEN=$'\e[32m'
YELLOW=$'\e[33m'
BLUE=$'\e[34m'
MAGENTA=$'\e[35m'
CYAN=$'\e[36m'
WHITE=$'\e[37m'
RESET=$'\e[0m'
ASK_COLOR=$GREEN
SEPARATOR_COLOR=$BLUE

git-claude-commit() {
  if git diff --cached --quiet; then
    echo "No changes staged. Please run 'git add' first."
    return $FALSE
  fi

  echo "Generating English commit message using Claude..."

  local prompt="Analyze the following git diff and generate a clear, concise git commit message in English following Conventional Commits format (e.g., feat: add login feature). Output ONLY the final commit message text. Absolutely NO explanations, NO greetings, and NO markdown code blocks."
  local msg
  msg=$(git diff --cached | claude -p "$prompt")

  if [ -n "$msg" ]; then
    echo -e "\ncommit message:\n$msg\n"
    git commit -m "$msg"
    local branch
    branch=$(git branch --show-current)
    if [ -z "$branch" ]; then
      echo "Error: Failed to detect current branch."
      return $FALSE
    fi
    git push -u origin "$branch"
  else
    echo "Error: Failed to generate commit message."
    return $FALSE
  fi
}

no-any-change() {
  local target_dir=$1
  local save_dir
  save_dir=$(pwd)

  cd "$target_dir"

  if [[ -z "$(git status --porcelain)" ]]; then
    cd "$save_dir"
  else
    cd "$save_dir"
    return $FALSE
  fi
}

has-staged-change() {
  ! git diff --cached --quiet
}

has-unstaged-change() {
  ! git diff --quiet || [[ -n "$(git ls-files --others --exclude-standard)" ]]
}


ask-to-commit() {
  TARGET_DIR=$1
  SAVE_DIR=$(pwd)
  if [[ -z "${TARGET_DIR}" ]]; then
    return $FALSE
  fi
  if [ -f "${TARGET_DIR}" ]; then
    TARGET_DIR=$(dirname "$TARGET_DIR")
  fi
  BASE_NAME=$(basename "$TARGET_DIR")

  if no-any-change "$TARGET_DIR"; then
    echo "Nothing to commit at ${BASE_NAME}"
    return $FALSE
  fi

  cd "$TARGET_DIR"
  echo
  echo -e "${SEPARATOR_COLOR}---[ ${TARGET_DIR} ]--------------${RESET}"
  git status
  echo

  if has-unstaged-change; then
    read -n1 -p "${ASK_COLOR}Git add all unstaged changes at ${BASE_NAME}? (y/N):${RESET}" yn
    echo
    if [[ $yn = [yY] ]]; then
      git add -A
      git status
      echo
      git-claude-commit
      cd "$SAVE_DIR"
      return
    fi
  fi

  if ! has-staged-change; then
    echo "No staged changes at ${BASE_NAME}"
    cd "$SAVE_DIR"
    return $FALSE
  fi

  read -n1 -p "${ASK_COLOR}Git commit at ${BASE_NAME} directory? (y/N):${RESET}" yn
  echo
  if [[ $yn = [yY] ]]; then
    git-claude-commit
    cd "$SAVE_DIR"
  else
    echo "skip committing"
    echo $FALSE
    cd "$SAVE_DIR"
  fi
}

ask-to-commit "${ROOT_DIR}/js-sdk-core"
ask-to-commit "${ROOT_DIR}/js-sdk-react"
ask-to-commit "${ROOT_DIR}/js-sdk-reactnative"
ask-to-commit "${ROOT_DIR}/react-for-googlemaps"
ask-to-commit "${ROOT_DIR}/react-for-maplibre"
ask-to-commit "${ROOT_DIR}/reactnative-for-googlemaps"
ask-to-commit "${ROOT_DIR}/reactnative-for-maplibre"
ask-to-commit "${ROOT_DIR}/"
