#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "${SCRIPT_DIR}")"

DRY_RUN=0
MODE="set"
VERSION=""
DISTRIBUTION_TYPE="bin"

usage() {
  cat <<'USAGE'
Usage:
  scripts/update-gradle-wrappers.sh <gradle-version> [options]
  scripts/update-gradle-wrappers.sh --sync-from-root [options]
  scripts/update-gradle-wrappers.sh --list

Update distributionUrl in every gradle/wrapper/gradle-wrapper.properties file.

Options:
      --sync-from-root      Copy the Gradle version and distribution type from the
                            root wrapper into module wrappers.
      --type <bin|all>      Distribution archive type. Defaults to bin.
      --list                List wrapper files and their current Gradle versions.
      --dry-run             Print files that would change without editing.
  -h, --help                Show this help.

Examples:
  scripts/update-gradle-wrappers.sh 9.5.1
  scripts/update-gradle-wrappers.sh 9.5.1 --type all --dry-run
  scripts/update-gradle-wrappers.sh --sync-from-root
USAGE
}

log() {
  printf '%s\n' "$*"
}

wrapper_files() {
  find "${ROOT_DIR}" \
    -path '*/build' -prune -o \
    -path '*/.gradle' -prune -o \
    -path '*/gradle/wrapper/gradle-wrapper.properties' -type f -print |
    sort
}

wrapper_distribution() {
  local file="$1"

  awk -F= '$1 == "distributionUrl" { print $2; exit }' "${file}"
}

wrapper_version() {
  local file="$1"
  local url

  url="$(wrapper_distribution "${file}")"
  case "${url}" in
    *gradle-*-bin.zip|*gradle-*-all.zip)
      printf '%s\n' "${url}" |
        perl -ne 'if (/gradle-([^-]+)-(?:bin|all)\.zip/) { print "$1\n"; exit }'
      ;;
  esac
}

wrapper_type() {
  local file="$1"
  local url

  url="$(wrapper_distribution "${file}")"
  case "${url}" in
    *-bin.zip)
      printf 'bin\n'
      ;;
    *-all.zip)
      printf 'all\n'
      ;;
  esac
}

validate_version() {
  local version="$1"

  if [[ ! "${version}" =~ ^[0-9]+(\.[0-9]+)*([.-][A-Za-z0-9]+)*$ ]]; then
    log "Invalid Gradle version: ${version}"
    exit 1
  fi
}

validate_type() {
  case "$1" in
    bin|all)
      ;;
    *)
      log "Invalid distribution type: $1"
      exit 1
      ;;
  esac
}

list_wrappers() {
  local file
  local version
  local type

  while IFS= read -r file; do
    version="$(wrapper_version "${file}")"
    type="$(wrapper_type "${file}")"
    if [[ -n "${version}" && -n "${type}" ]]; then
      log "${file#${ROOT_DIR}/}: ${version} (${type})"
    else
      log "${file#${ROOT_DIR}/}: unsupported distributionUrl"
    fi
  done < <(wrapper_files)
}

update_file() {
  local file="$1"
  local version="$2"
  local type="$3"
  local current_version
  local current_type

  current_version="$(wrapper_version "${file}")"
  current_type="$(wrapper_type "${file}")"

  if [[ -z "${current_version}" || -z "${current_type}" ]]; then
    log "Skipping ${file#${ROOT_DIR}/}: unsupported distributionUrl"
    return 1
  fi

  if [[ "${current_version}" == "${version}" && "${current_type}" == "${type}" ]]; then
    log "unchanged ${file#${ROOT_DIR}/}: ${version} (${type})"
    return 0
  fi

  log "update    ${file#${ROOT_DIR}/}: ${current_version} (${current_type}) -> ${version} (${type})"
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    return 0
  fi

  if ! perl -0pi -e \
    'BEGIN { ($version, $type) = splice(@ARGV, 0, 2) } s#^(distributionUrl=.*?/gradle-)[^-]+-(bin|all)(\.zip)$#${1}${version}-${type}${3}#mg' \
    "${version}" "${type}" "${file}"; then
    log "Failed to update ${file#${ROOT_DIR}/}"
    exit 1
  fi
}

parse_args() {
  if [[ $# -eq 0 ]]; then
    usage
    exit 1
  fi

  case "$1" in
    --list)
      MODE="list"
      shift
      ;;
    --sync-from-root)
      MODE="sync"
      shift
      ;;
  esac

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --dry-run)
        DRY_RUN=1
        shift
        ;;
      --type)
        DISTRIBUTION_TYPE="${2:-}"
        if [[ -z "${DISTRIBUTION_TYPE}" ]]; then
          log "Missing value for --type"
          exit 1
        fi
        validate_type "${DISTRIBUTION_TYPE}"
        shift 2
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
        if [[ "${MODE}" != "set" || -n "${VERSION}" ]]; then
          log "Unexpected argument: $1"
          usage
          exit 1
        fi
        VERSION="$1"
        shift
        ;;
    esac
  done
}

main() {
  local file
  local matched=0
  parse_args "$@"

  if [[ "${MODE}" == "list" ]]; then
    list_wrappers
    exit 0
  fi

  if [[ "${MODE}" == "sync" ]]; then
    local root_wrapper="${ROOT_DIR}/gradle/wrapper/gradle-wrapper.properties"
    VERSION="$(wrapper_version "${root_wrapper}")"
    DISTRIBUTION_TYPE="$(wrapper_type "${root_wrapper}")"

    if [[ -z "${VERSION}" || -z "${DISTRIBUTION_TYPE}" ]]; then
      log "Root wrapper has an unsupported distributionUrl."
      exit 1
    fi

    while IFS= read -r file; do
      [[ "${file}" == "${root_wrapper}" ]] && continue
      if update_file "${file}" "${VERSION}" "${DISTRIBUTION_TYPE}"; then
        matched=1
      fi
    done < <(wrapper_files)
  else
    if [[ -z "${VERSION}" ]]; then
      usage
      exit 1
    fi

    validate_version "${VERSION}"
    validate_type "${DISTRIBUTION_TYPE}"

    while IFS= read -r file; do
      if update_file "${file}" "${VERSION}" "${DISTRIBUTION_TYPE}"; then
        matched=1
      fi
    done < <(wrapper_files)
  fi

  if [[ "${matched}" -eq 0 ]]; then
    log "No supported Gradle wrapper files found."
    exit 1
  fi
}

main "$@"
