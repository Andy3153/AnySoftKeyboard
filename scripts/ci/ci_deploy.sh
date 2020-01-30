#!/usr/bin/env bash
set -e

KEYSTORE_FILE_URL=$1
shift
PUBLISH_CERT_FILE_URL=$1
shift
DEPLOY_TASKS="$*"

echo "Deploy with tasks: '${DEPLOY_TASKS}'"

if [[ "${DEPLOY_TASKS}" == *"publish"* ]]; then
    echo "Downloading signature files..."

    if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
        echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
        exit 1
    fi

    if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
        echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
        exit 1
    fi

    wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
    wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
fi

./gradlew --stacktrace -PwithAutoVersioning ${DEPLOY_TASKS} generateFdroidYamls

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .