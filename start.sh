#!/usr/bin/env bash
# SLSMS - one-shot dev launcher (macOS / Linux)
# Spawns Spring Boot backend on :8080 and Vite frontend on :5173 in parallel.
# Stop both with Ctrl-C.

set -e
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

cleanup() {
  echo
  echo "[start] shutting down…"
  kill 0 2>/dev/null || true
}
trap cleanup INT TERM EXIT

# Backend
(
  cd "$ROOT_DIR/backend"
  echo "[backend] starting Spring Boot on http://localhost:8080 …"
  if command -v mvn >/dev/null 2>&1; then
    mvn -q spring-boot:run
  else
    echo "[backend] ERROR: Maven not found. Install with: brew install maven   (macOS)"
    echo "[backend] or use IntelliJ IDEA to run SlsmsApplication.java instead."
    exit 1
  fi
) &

# Frontend
(
  cd "$ROOT_DIR/frontend"
  echo "[frontend] starting Vite on http://localhost:5173 …"
  if [ ! -d node_modules ]; then
    echo "[frontend] running npm install (first time only)…"
    npm install
  fi
  npm run dev
) &

wait
