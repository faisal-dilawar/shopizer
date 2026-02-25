#!/usr/bin/env bash
# =============================================================================
# shopizer/stop.sh — Backend (Spring Boot) Stop Script
# Stops the Spring Boot app and MySQL (pass --keep-db to leave MySQL running)
# =============================================================================

set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/.backend.pid"

KEEP_DB=false
[[ "${1:-}" == "--keep-db" ]] && KEEP_DB=true

log() { echo "[$(date '+%H:%M:%S')] $*"; }

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║  Shopizer Backend — Stop                         ║"
echo "╚══════════════════════════════════════════════════╝"

# ── Stop Spring Boot ──────────────────────────────────────────────────────────

echo ""
echo "── Spring Boot ─────────────────────────────────────────"

STOPPED=false

if [[ -f "$PID_FILE" ]]; then
  PID=$(cat "$PID_FILE")
  if kill -0 "$PID" 2>/dev/null; then
    log "Stopping backend (PID: $PID)..."
    kill "$PID" 2>/dev/null || true
    sleep 3

    if kill -0 "$PID" 2>/dev/null; then
      log "Process still alive — sending SIGKILL..."
      kill -9 "$PID" 2>/dev/null || true
    fi
    STOPPED=true
  else
    log "PID $PID from .backend.pid is no longer running"
  fi
  rm -f "$PID_FILE"
fi

# Fallback: kill anything on port 8080
PORT_PIDS=$(lsof -ti:8080 2>/dev/null || true)
if [[ -n "$PORT_PIDS" ]]; then
  log "Killing processes on port 8080: $PORT_PIDS"
  echo "$PORT_PIDS" | xargs kill -9 2>/dev/null || true
  STOPPED=true
fi

$STOPPED && log "✓ Spring Boot stopped" || log "✓ Spring Boot was not running"

# ── Stop MySQL ────────────────────────────────────────────────────────────────

echo ""
echo "── MySQL ────────────────────────────────────────────────"

if $KEEP_DB; then
  log "Skipping MySQL stop (--keep-db flag set)"
else
  if command -v brew &>/dev/null && brew list mysql &>/dev/null 2>&1; then
    MYSQL_STATUS=$(brew services list | awk '/^mysql / {print $2}')
    if [[ "$MYSQL_STATUS" == "started" ]]; then
      log "Stopping MySQL..."
      brew services stop mysql \
        && log "✓ MySQL stopped" \
        || log "⚠ Failed to stop MySQL — stop it manually: brew services stop mysql"
    else
      log "✓ MySQL was not running"
    fi
  else
    log "MySQL not managed by Homebrew — skipping"
  fi
fi

echo ""
log "Backend shutdown complete."
echo ""
echo "Tip: To stop only the app and keep MySQL running:"
echo "  $SCRIPT_DIR/stop.sh --keep-db"
