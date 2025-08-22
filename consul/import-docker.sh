#!/bin/sh
# language: bash
# import-docker.sh
# Импортирует /configs/docker -> Consul keys: config/{service}/data
set -e

CONSUL_ADDR="${CONSUL_ADDR:-http://consul:8500}"
ENV_NAME="docker"
ROOT_DIR="${ROOT_DIR:-/configs}"
WAIT_TIMEOUT="${CONSUL_WAIT_TIMEOUT:-60}"
WAIT_INTERVAL="${CONSUL_WAIT_INTERVAL:-2}"

echo "Import (docker) ${ROOT_DIR}/${ENV_NAME} -> ${CONSUL_ADDR}/v1/kv/config/{service}/data"

wait_for_consul() {
  local waited=0
  while :; do
    leader=$(curl -s --max-time 2 "${CONSUL_ADDR}/v1/status/leader" || true)
    if [ -n "$leader" ] && [ "$leader" != "null" ]; then
      echo "Consul leader: ${leader}"
      return 0
    fi
    if [ "$waited" -ge "$WAIT_TIMEOUT" ]; then
      echo "Consul not ready after ${WAIT_TIMEOUT}s" >&2
      return 1
    fi
    sleep "$WAIT_INTERVAL"
    waited=$((waited + WAIT_INTERVAL))
  done
}

if ! wait_for_consul; then
  exit 2
fi

if [ ! -d "${ROOT_DIR}/${ENV_NAME}" ]; then
  echo "Directory ${ROOT_DIR}/${ENV_NAME} not found, nothing to import."
  exit 0
fi

cd "${ROOT_DIR}/${ENV_NAME}" || exit 0

services=$( (for f in *; do
  if [ -f "$f" ]; then
    case "$f" in
      *.yml|*.yaml|*.properties) printf "%s\n" "${f%.*}";;
    esac
  elif [ -d "$f" ]; then
    printf "%s\n" "$f"
  fi
done) | sort -u )

if [ -z "$services" ]; then
  echo "No services found under ${ROOT_DIR}/${ENV_NAME}"
  exit 0
fi

for svc in $services; do
  echo "-> processing service '${svc}'"
  tmpfile=$(mktemp)

  if [ -f "${ROOT_DIR}/${ENV_NAME}/${svc}.yml" ]; then
    cat "${ROOT_DIR}/${ENV_NAME}/${svc}.yml" > "${tmpfile}"
  elif [ -f "${ROOT_DIR}/${ENV_NAME}/${svc}.yaml" ]; then
    cat "${ROOT_DIR}/${ENV_NAME}/${svc}.yaml" > "${tmpfile}"
  elif [ -f "${ROOT_DIR}/${ENV_NAME}/${svc}.properties" ]; then
    cat "${ROOT_DIR}/${ENV_NAME}/${svc}.properties" > "${tmpfile}"
  elif [ -d "${ROOT_DIR}/${ENV_NAME}/${svc}" ]; then
    find "${ROOT_DIR}/${ENV_NAME}/${svc}" -type f | sort | while read -r f; do
      echo "---" >> "${tmpfile}"
      cat "$f" >> "${tmpfile}"
      echo "" >> "${tmpfile}"
    done
  else
    echo "No source files for service ${svc}, skipping"
    rm -f "${tmpfile}"
    continue
  fi

  key="config/${svc}/data"
  if curl -sS --fail -X PUT --data-binary @"${tmpfile}" "${CONSUL_ADDR}/v1/kv/${key}"; then
    echo "PUT ${key}"
  else
    echo "Failed PUT ${key}" >&2
  fi
  rm -f "${tmpfile}"
done

echo "Import (docker) finished."