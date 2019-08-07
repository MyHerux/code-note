#!/bin/bash
set -eo pipefail
shopt -s nullglob

MY_PORT="${PORT:=3000}"
MY_ACOUNT="${ADMIN_EMAIL}"
MY_DB_SERVER="${DB_SERVER}"
MY_DB_NAME="${DB_NAME}"
MY_DB_PORT="${DB_PORT}"
MY_USER="${DB_USER}"
My_PASS="${DB_PASS}"
MY_AUTH="${DB_AUTH}"

config() {
    if [[ -z "${MY_PORT}" || -z "${MY_ACOUNT}" || -z "${MY_DB_SERVER}" || -z "${MY_DB_NAME}" || -z "${MY_DB_PORT}" || -z "${MY_USER}" || -z "${My_PASS}" || -z "${MY_AUTH}" ]]; then
        echo -e "\n\"MY_PORT\" or \"MY_ACOUNT\" or \"MY_DB_SERVER\" or \"MY_DB_NAME\" or \"MY_DB_PORT\" or \"MY_USER\" or \"My_PASS\" or \"MY_AUTH\" can not be empty!\n" && exit 1
    else
        sed -i "s#MY_PORT#${MY_PORT}#g"             /api/config.json
        sed -i "s#MY_ACOUNT#${MY_ACOUNT}#g"         /api/config.json
        sed -i "s#MY_DB_SERVER#${MY_DB_SERVER}#g"         /api/config.json
        sed -i "s#MY_DB_NAME#${MY_DB_NAME}#g"         /api/config.json
        sed -i "s#MY_DB_PORT#${MY_DB_PORT}#g"         /api/config.json
        sed -i "s#MY_USER#${MY_USER}#g"         /api/config.json
        sed -i "s#My_PASS#${My_PASS}#g"         /api/config.json
        sed -i "s#MY_AUTH#${MY_AUTH}#g"         /api/config.json
    
    fi
}

config

node /api/vendors/server/app.js

exec "$@"