#!/usr/bin/env bash
# =============================================================================
# shopizer/start.sh — Backend (Spring Boot) Start Script
# Handles first-time setup: Homebrew, SDKMAN, Java 17, Maven, MySQL
# =============================================================================

set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOGS_DIR="$SCRIPT_DIR/logs"
LOG_FILE="$LOGS_DIR/backend.log"
PID_FILE="$SCRIPT_DIR/.backend.pid"
DB_PROPS="$SCRIPT_DIR/sm-shop/src/main/resources/database.properties"
JAVA_VERSION="17.0.17-amzn"

mkdir -p "$LOGS_DIR"

# ── Helpers ───────────────────────────────────────────────────────────────────

log()     { echo "[$(date '+%H:%M:%S')] $*"; }
section() { echo ""; echo "── $1 ──────────────────────────────────────────"; }

fatal() {
  local msg="$1" cmd="${2:-N/A}" code="${3:-1}"
  echo ""
  echo "╔══════════════════════════════════════════════════╗"
  echo "║  ✗ BACKEND START FAILED                          ║"
  echo "╚══════════════════════════════════════════════════╝"
  echo "  Error   : $msg"
  echo "  Command : $cmd"
  echo "  Exit    : $code"
  echo "  Log     : $LOG_FILE"
  echo ""
  echo "┄┄┄┄┄┄ PASTE THE BLOCK BELOW INTO CLAUDE CODE CLI TO FIX ┄┄┄┄┄┄"
  echo ""
  echo "The script shopizer/start.sh failed. Please fix it."
  echo "Error: $msg"
  echo "Failed command: $cmd"
  echo "Exit code: $code"
  echo "OS: $(uname -s) $(uname -m)"
  echo "Shell: ${SHELL:-unknown}"
  echo "Script: $SCRIPT_DIR/start.sh"
  echo "Recent log:"
  tail -20 "$LOG_FILE" 2>/dev/null || echo "(no log yet)"
  echo ""
  echo "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
  exit 1
}

# ── Homebrew ──────────────────────────────────────────────────────────────────

setup_brew() {
  section "Homebrew"
  if ! command -v brew &>/dev/null; then
    log "Homebrew not found. Installing..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" \
      || fatal "Failed to install Homebrew" "homebrew install script"
    # Add to PATH for M1 and Intel Macs
    [[ -f /opt/homebrew/bin/brew ]] && eval "$(/opt/homebrew/bin/brew shellenv)"
    [[ -f /usr/local/bin/brew   ]] && eval "$(/usr/local/bin/brew shellenv)"
  fi
  log "✓ $(brew --version | head -1)"
}

# ── SDKMAN ────────────────────────────────────────────────────────────────────

setup_sdkman() {
  section "SDKMAN"
  if [[ ! -d "$HOME/.sdkman" ]]; then
    log "SDKMAN not found. Installing..."
    curl -s "https://get.sdkman.io" | bash \
      || fatal "Failed to install SDKMAN" "curl https://get.sdkman.io | bash"
  fi
  export SDKMAN_DIR="$HOME/.sdkman"
  # shellcheck disable=SC1091
  source "$SDKMAN_DIR/bin/sdkman-init.sh" \
    || fatal "Failed to load SDKMAN" "source \$SDKMAN_DIR/bin/sdkman-init.sh"
  log "✓ SDKMAN loaded"
}

# ── Java 17 ───────────────────────────────────────────────────────────────────

setup_java() {
  section "Java 17 ($JAVA_VERSION)"
  export SDKMAN_DIR="$HOME/.sdkman"
  source "$SDKMAN_DIR/bin/sdkman-init.sh"

  if [[ ! -d "$HOME/.sdkman/candidates/java/$JAVA_VERSION" ]]; then
    log "Java $JAVA_VERSION not found. Installing via SDKMAN..."
    sdk install java "$JAVA_VERSION" </dev/null \
      || fatal "Failed to install Java $JAVA_VERSION" "sdk install java $JAVA_VERSION"
  fi

  sdk use java "$JAVA_VERSION" </dev/null \
    || fatal "Failed to switch to Java $JAVA_VERSION" "sdk use java $JAVA_VERSION"

  log "✓ $(java -version 2>&1 | head -1)"
}

# ── Maven ─────────────────────────────────────────────────────────────────────

setup_maven() {
  section "Maven"
  export SDKMAN_DIR="$HOME/.sdkman"
  source "$SDKMAN_DIR/bin/sdkman-init.sh"

  if ! command -v mvn &>/dev/null; then
    log "Maven not found. Installing via SDKMAN..."
    sdk install maven </dev/null \
      || fatal "Failed to install Maven" "sdk install maven"
  fi
  log "✓ $(mvn -version | head -1)"
}

# ── database.properties ───────────────────────────────────────────────────────

check_db_properties() {
  section "database.properties"
  if [[ ! -f "$DB_PROPS" ]]; then
    echo ""
    echo "╔══════════════════════════════════════════════════╗"
    echo "║  ✗ MISSING: database.properties                  ║"
    echo "╚══════════════════════════════════════════════════╝"
    echo ""
    echo "  Expected at: $DB_PROPS"
    echo ""
    echo "  This file is excluded from git (see .gitignore)."
    echo "  Create it manually using the template below, then re-run this script."
    echo ""
    echo "  ── Copy-paste to create the file ─────────────────────────────"
    echo ""
    echo "  cat > $DB_PROPS << 'DBEOF'"
    echo "  db.jdbcUrl=jdbc:mysql://127.0.0.1:3306/SALESMANAGER?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
    echo "  db.user=root"
    echo "  db.password=YOUR_MYSQL_ROOT_PASSWORD"
    echo "  db.driverClass=com.mysql.cj.jdbc.Driver"
    echo "  hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect"
    echo "  db.preferredTestQuery=SELECT 1"
    echo "  db.show.sql=false"
    echo "  db.schema=SALESMANAGER"
    echo "  hibernate.hbm2ddl.auto=update"
    echo "  db.initialPoolSize=4"
    echo "  db.minPoolSize=4"
    echo "  db.maxPoolSize=4"
    echo "  DBEOF"
    echo ""
    echo "  ───────────────────────────────────────────────────────────────"
    echo ""
    exit 1
  fi
  log "✓ database.properties exists"
}

# ── MySQL ─────────────────────────────────────────────────────────────────────

start_mysql() {
  section "MySQL"
  if ! command -v brew &>/dev/null; then
    log "⚠ Homebrew not available — skipping MySQL auto-start"
    log "  Ensure MySQL is running on port 3306"
    return
  fi

  if ! brew list mysql &>/dev/null 2>&1; then
    log "MySQL not installed. Installing via Homebrew..."
    brew install mysql \
      || fatal "Failed to install MySQL" "brew install mysql"
  fi

  MYSQL_STATUS=$(brew services list | awk '/^mysql / {print $2}')
  if [[ "$MYSQL_STATUS" == "started" ]]; then
    log "✓ MySQL already running"
    return
  fi

  log "Starting MySQL (status: ${MYSQL_STATUS:-unknown})..."
  brew services start mysql \
    || fatal "Failed to start MySQL" "brew services start mysql"

  log "Waiting for MySQL to be ready..."
  for i in {1..15}; do
    if mysqladmin ping --silent 2>/dev/null; then
      log "✓ MySQL ready"
      return
    fi
    sleep 2
    log "  Still waiting... ($i/15)"
  done
  log "⚠ MySQL started but did not respond to ping. The app will attempt to connect anyway."
}

# ── Spring Boot ───────────────────────────────────────────────────────────────

start_backend() {
  section "Starting Spring Boot"

  # Stop any existing instance
  if [[ -f "$PID_FILE" ]]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
      log "Stopping existing backend (PID: $OLD_PID)..."
      kill "$OLD_PID" 2>/dev/null || true
      sleep 3
    fi
    rm -f "$PID_FILE"
  fi

  # Kill anything still on port 8080
  lsof -ti:8080 | xargs kill -9 2>/dev/null || true

  log "Running: mvn spring-boot:run -pl sm-shop"
  log "Logging to: $LOG_FILE"

  cd "$SCRIPT_DIR"
  mvn spring-boot:run -pl sm-shop >> "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"

  log "✓ Backend started (PID: $(cat "$PID_FILE"))"
  log "  URL  : http://localhost:8080"
  log "  Logs : tail -f $LOG_FILE"
  log "  Stop : $SCRIPT_DIR/stop.sh"
}

# ── Main ──────────────────────────────────────────────────────────────────────

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║  Shopizer Backend — Setup & Start                ║"
echo "╚══════════════════════════════════════════════════╝"

setup_brew
setup_sdkman
setup_java
setup_maven
check_db_properties
start_mysql
start_backend

echo ""
log "Done. Backend is starting up in the background."
log "It may take 30-60 seconds for the app to be fully ready."
