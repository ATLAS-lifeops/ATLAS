#!/bin/sh
set -eu

APP_URL="${ATLAS_APP_URL:-http://localhost:8080}"
HEALTH_URL="$APP_URL/actuator/health"
SETUP_URL="$APP_URL/setup"

fail() {
  printf '%s\n' "$1" >&2
  exit 1
}

command -v docker >/dev/null 2>&1 || fail "Docker is required to start ATLAS."
docker compose version >/dev/null 2>&1 || fail "Docker Compose is required to start ATLAS."

docker compose up --build -d

printf '%s\n' "Waiting for ATLAS at $HEALTH_URL ..."

attempts=60
while [ "$attempts" -gt 0 ]; do
  if command -v curl >/dev/null 2>&1; then
    if curl -fsS "$HEALTH_URL" >/dev/null 2>&1; then
      break
    fi
  elif command -v wget >/dev/null 2>&1; then
    if wget -qO- "$HEALTH_URL" >/dev/null 2>&1; then
      break
    fi
  else
    fail "curl or wget is required to wait for ATLAS health."
  fi
  attempts=$((attempts - 1))
  sleep 2
done

[ "$attempts" -gt 0 ] || fail "ATLAS did not become healthy. Run: docker compose logs -f atlas-app"

opened=false
case "$(uname -s 2>/dev/null || printf unknown)" in
  Darwin*)
    if command -v open >/dev/null 2>&1; then
      open "$SETUP_URL" >/dev/null 2>&1 && opened=true
    fi
    ;;
  Linux*)
    if command -v xdg-open >/dev/null 2>&1; then
      xdg-open "$SETUP_URL" >/dev/null 2>&1 && opened=true
    elif command -v cmd.exe >/dev/null 2>&1; then
      cmd.exe /c start "$SETUP_URL" >/dev/null 2>&1 && opened=true
    fi
    ;;
  MINGW*|MSYS*|CYGWIN*)
    if command -v cmd.exe >/dev/null 2>&1; then
      cmd.exe /c start "$SETUP_URL" >/dev/null 2>&1 && opened=true
    fi
    ;;
esac

printf '%s\n' "ATLAS is running."
printf '%s\n' "Open setup: $SETUP_URL"

if [ "$opened" != "true" ]; then
  printf '%s\n' "Browser opening was unavailable. Open the setup URL manually."
fi
