#!/usr/bin/env bash

set -u

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
UPSTREAM_DIR="${UPSTREAM_DIR:-$ROOT_DIR/workplace/miku-xlsx2md}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/target/node-java-markdown-compare}"
JAVA_JAR="${JAVA_JAR:-}"

DEFAULT_FIXTURES=(
  "xlsx2md-basic-sample01.xlsx"
  "link/hyperlink-basic-sample01.xlsx"
)

COMMON_OPTIONS=(
  "--formatting-mode" "github"
)

failures=0

log() {
  printf '%s\n' "$*"
}

error() {
  printf 'error: %s\n' "$*" >&2
}

sanitize_name() {
  printf '%s' "$1" | tr '/.' '__'
}

find_java_jar() {
  if [ -n "$JAVA_JAR" ]; then
    printf '%s\n' "$JAVA_JAR"
    return 0
  fi

  for candidate in "$ROOT_DIR"/miku-xlsx2md/target/miku-xlsx2md-*.jar; do
    [ -f "$candidate" ] || continue
    case "$(basename "$candidate")" in
      original-*) continue ;;
    esac
    printf '%s\n' "$candidate"
    return 0
  done

  return 1
}

if [ ! -d "$UPSTREAM_DIR" ]; then
  error "upstream workspace is not available: $UPSTREAM_DIR"
  error "clone upstream into workplace/miku-xlsx2md first."
  exit 2
fi

if [ ! -d "$UPSTREAM_DIR/node_modules" ]; then
  error "upstream node_modules is not available: $UPSTREAM_DIR/node_modules"
  error "run npm install in workplace/miku-xlsx2md before comparing Node output."
  exit 2
fi

java_jar="$(find_java_jar)"
if [ ! -f "$java_jar" ]; then
  error "Java CLI jar is not available."
  error "run mvn package first, or set JAVA_JAR to the jar path."
  exit 2
fi

if [ "$#" -gt 0 ]; then
  fixtures=("$@")
else
  fixtures=("${DEFAULT_FIXTURES[@]}")
fi

mkdir -p "$OUT_DIR"

log "Node / Java Markdown byte-level comparison"
log "upstream: $UPSTREAM_DIR"
log "java jar: $java_jar"
log "output: $OUT_DIR"
log ""

for fixture in "${fixtures[@]}"; do
  fixture_path="$UPSTREAM_DIR/tests/fixtures/$fixture"
  case_name="$(sanitize_name "$fixture")"
  node_out="$OUT_DIR/$case_name.node.md"
  java_out="$OUT_DIR/$case_name.java.md"
  diff_out="$OUT_DIR/$case_name.diff"

  if [ ! -f "$fixture_path" ]; then
    error "fixture is not available: $fixture"
    failures=$((failures + 1))
    continue
  fi

  log "[compare] $fixture"

  if ! (cd "$UPSTREAM_DIR" && npm run cli -- "tests/fixtures/$fixture" --out "$node_out" "${COMMON_OPTIONS[@]}"); then
    error "Node CLI failed for $fixture"
    failures=$((failures + 1))
    continue
  fi

  if ! java -jar "$java_jar" "$fixture_path" --out "$java_out" "${COMMON_OPTIONS[@]}"; then
    error "Java CLI failed for $fixture"
    failures=$((failures + 1))
    continue
  fi

  if cmp -s "$node_out" "$java_out"; then
    rm -f "$diff_out"
    log "  ok: byte-identical"
  else
    diff -u --label "node:$fixture" --label "java:$fixture" "$node_out" "$java_out" > "$diff_out"
    error "Markdown differs for $fixture"
    error "diff: $diff_out"
    failures=$((failures + 1))
  fi
done

log ""
if [ "$failures" -eq 0 ]; then
  log "All compared Markdown outputs are byte-identical."
  exit 0
fi

error "$failures comparison(s) failed."
exit 1
