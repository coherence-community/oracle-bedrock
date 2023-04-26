#!/bin/bash -e

set -o pipefail || true  # trace ERR through pipes
set -o errtrace || true # trace ERR through commands and functions
set -o errexit || true  # exit the script if any statement returns a non-true return value

on_error(){
    CODE="${?}" && \
    set +x && \
    printf "[ERROR] Error(code=%s) occurred at %s:%s command: %s\n" \
        "${CODE}" "${BASH_SOURCE}" "${LINENO}" "${BASH_COMMAND}"
}
trap on_error ERR

# Path to this script
if [ -h "${0}" ] ; then
    readonly SCRIPT_PATH="$(readlink "${0}")"
else
    readonly SCRIPT_PATH="${0}"
fi

# Path to the root of the workspace
# shellcheck disable=SC2046
readonly WS_DIR=$(cd $(dirname -- "${SCRIPT_PATH}") ; cd .. ; pwd -P)

readonly RESULT_DIR="${WS_DIR}/target"
readonly RESULT_FILE="${RESULT_DIR}/dependency-check-result.txt"
mkdir -p "${RESULT_DIR}" || true

die(){ cat "${RESULT_FILE}" ; echo "Dependency report in ${WS_DIR}/target" ; echo "${1}" ; exit 1 ;}

echo "Running CVE check - results: ${RESULT_FILE}"
mvn ${MAVEN_ARGS} -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN org.owasp:dependency-check-maven:aggregate \
        -Dtop.parent.basedir="${WS_DIR}" -nsu \
        > "${RESULT_FILE}" || die "Error running the Maven command"

grep -i "One or more dependencies were identified with known vulnerabilities" "${RESULT_FILE}" \
    && die "CVE SCAN ERROR" || echo "CVE SCAN OK"
