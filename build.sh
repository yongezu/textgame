#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/src"
OUT="$ROOT/build"
LIB="$ROOT/lib"

rm -rf "$OUT"
mkdir -p "$OUT"

SOURCES=$(find "$SRC" -name '*.java')
if [ -z "$SOURCES" ]; then
  echo "No .java sources found under $SRC"
  exit 0
fi

javac -d "$OUT" -cp "$LIB/*" $SOURCES
echo "Build OK -> $OUT"
