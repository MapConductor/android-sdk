#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "${SCRIPT_DIR}")"

DRY_RUN=0
MODE="set"
KEY=""
VERSION=""
POSITIONAL=()

usage() {
  cat <<'USAGE'
Usage:
  scripts/update-version-catalogs.sh <version-key> <version> [options]
  scripts/update-version-catalogs.sh --sync-from-root <version-key> [<version-key> ...] [options]
  scripts/update-version-catalogs.sh --list

Update version entries in every gradle/libs.versions.toml file in this repo.

Options:
      --sync-from-root      Copy the version value from root gradle/libs.versions.toml
                            into module gradle/libs.versions.toml files.
      --list                List all version keys found in catalog files.
      --dry-run             Print files that would change without editing.
  -h, --help                Show this help.

Examples:
  scripts/update-version-catalogs.sh agp 9.2.2
  scripts/update-version-catalogs.sh composeBom 2026.06.00 --dry-run
  scripts/update-version-catalogs.sh --sync-from-root agp kotlin compose composeBom
USAGE
}

log() {
  printf '%s\n' "$*"
}

catalog_files() {
  find "${ROOT_DIR}" \
    -path '*/build' -prune -o \
    -path '*/.gradle' -prune -o \
    -path '*/gradle/libs.versions.toml' -type f -print |
    sort
}

version_keys() {
  awk '
    /^\[versions\]/ { in_versions = 1; next }
    /^\[/ { in_versions = 0 }
    in_versions && /^[[:space:]]*[A-Za-z0-9_.-]+[[:space:]]*=/ {
      key = $0
      sub(/^[[:space:]]*/, "", key)
      sub(/[[:space:]]*=.*/, "", key)
      print key
    }
  ' "$@"
}

version_value() {
  local file="$1"
  local key="$2"

  awk -v key="${key}" '
    /^\[versions\]/ { in_versions = 1; next }
    /^\[/ { in_versions = 0 }
    in_versions {
      line = $0
      sub(/^[[:space:]]*/, "", line)
      if (line ~ "^" key "[[:space:]]*=") {
        sub(/^[^=]*=[[:space:]]*"/, "", line)
        sub(/".*/, "", line)
        print line
        exit
      }
    }
  ' "${file}"
}

update_file() {
  local file="$1"
  local key="$2"
  local version="$3"
  local current

  current="$(version_value "${file}" "${key}")"
  if [[ -z "${current}" ]]; then
    return 1
  fi

  if [[ "${current}" == "${version}" ]]; then
    log "unchanged ${file#${ROOT_DIR}/}: ${key}=${version}"
    return 0
  fi

  log "update    ${file#${ROOT_DIR}/}: ${key} ${current} -> ${version}"
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    return 0
  fi

  perl -0pi -e \
    'BEGIN { ($key, $version) = splice(@ARGV, 0, 2) } s/^([ \t]*\Q$key\E[ \t]*=[ \t]*")[^"]*(")/$1$version$2/mg' \
    "${key}" "${version}" "${file}"
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
        if [[ "${MODE}" == "set" ]]; then
          if [[ -z "${KEY}" ]]; then
            KEY="$1"
          elif [[ -z "${VERSION}" ]]; then
            VERSION="$1"
          else
            log "Unexpected argument: $1"
            usage
            exit 1
          fi
        else
          POSITIONAL+=("$1")
        fi
        shift
        ;;
    esac
  done
}

main() {
  local -a files
  parse_args "$@"
  while IFS= read -r file; do
    files+=("${file}")
  done < <(catalog_files)

  if [[ "${MODE}" == "list" ]]; then
    version_keys "${files[@]}" | sort -u
    exit 0
  fi

  if [[ "${MODE}" == "set" ]]; then
    if [[ -z "${KEY}" || -z "${VERSION}" ]]; then
      usage
      exit 1
    fi

    local matched=0
    for file in "${files[@]}"; do
      if update_file "${file}" "${KEY}" "${VERSION}"; then
        matched=1
      fi
    done

    if [[ "${matched}" -eq 0 ]]; then
      log "No catalog defines version key: ${KEY}"
      exit 1
    fi
    exit 0
  fi

  if [[ "${MODE}" == "sync" ]]; then
    local root_catalog="${ROOT_DIR}/gradle/libs.versions.toml"
    local key
    local version

    if [[ "${#POSITIONAL[@]}" -eq 0 ]]; then
      log "Pass at least one version key to --sync-from-root."
      usage
      exit 1
    fi

    for key in "${POSITIONAL[@]}"; do
      version="$(version_value "${root_catalog}" "${key}")"
      if [[ -z "${version}" ]]; then
        log "Root catalog does not define version key: ${key}"
        exit 1
      fi

      local matched=0
      for file in "${files[@]}"; do
        [[ "${file}" == "${root_catalog}" ]] && continue
        if update_file "${file}" "${key}" "${version}"; then
          matched=1
        fi
      done

      if [[ "${matched}" -eq 0 ]]; then
        log "No module catalog defines version key: ${key}"
        exit 1
      fi
    done
  fi
}

main "$@"
