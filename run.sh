#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/build"
LIB="$ROOT/lib"

MAIN="${1:-basketball.Main}"

./build.sh

java -cp "$OUT:$LIB/*" "$MAIN"
