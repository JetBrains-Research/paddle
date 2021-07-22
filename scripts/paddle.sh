#!/usr/bin/env bash

APP_NAME=Paddle

LATEST_BUILD_INFO=$(curl -s https://api.github.com/repos/tanvd/paddle/releases/latest)
CHANGELOG_URL='https://github.com/tanvd/paddle/blob/master/CHANGELOG.md'
PADDLE_HOME=$HOME/.local/share/paddle
FIRST_ARG=$1

check_prerequisites () {
    local required_utils="java curl"

    for util in ${required_utils}
    do
        which ${util} > /dev/null
        if [[ $? -eq 1 ]]; then
            echo "\"$util\" has to be installed to use the wrapper"
            exit 1
        fi
    done
}

check_config () {
    [[ ! -d ${PADDLE_HOME} ]] && mkdir -p ${PADDLE_HOME}
    [[ ! -f ${PADDLE_HOME}/version ]] && touch ${PADDLE_HOME}/version
}

check_for_updates () {
    local cur_ver=$(cat ${PADDLE_HOME}/version)
    local latest_ver=$(echo ${LATEST_BUILD_INFO} | jq -r '.name')
    local jar_name=${PADDLE_HOME}/${APP_NAME}.jar

    if [[ ${latest_ver} == "" || ${latest_ver} == "null" ]]; then
        echo "WARNING: Couldn't connect to GitHub to check for updates."
        return
    fi

    if [[ ${cur_ver} == "" || ${cur_ver} != ${latest_ver} || ! -f ${jar_name} || (${FIRST_ARG} == "check-updates" && ${cur_ver} != ${latest_ver}) ]]; then
        if [[ ${latest_ver} == $(cat ${PADDLE_HOME}/skip-update 2> /dev/null) && ${FIRST_ARG} != "check-updates" ]]
        then
            echo "You are using ${APP_NAME} ${cur_ver}, newer version ($(cat ${PADDLE_HOME}/skip-update)) is available but was skipped."
            return
        fi

        echo "New version of ${APP_NAME} is available - ${latest_ver}"
        echo "CHANGELOG: $CHANGELOG_URL"
        echo "Would you like to update? (yes/no):"

        read confirmation
        if [[ ${confirmation} == "yes" ]]; then
            echo "Updating..."
            curl $(echo ${LATEST_BUILD_INFO} | jq -r '.assets[].browser_download_url') -L -s -o ${jar_name} && echo ${latest_ver} > ${PADDLE_HOME}/version
            echo "Updated!"
        else
            echo "${latest_ver}" > ${PADDLE_HOME}/skip-update

            echo "Update to ${latest_ver} skipped."
            echo "You can always check for updates by running \"${APP_NAME} check-updates\""
        fi
    else
        echo "You are using latest version of ${APP_NAME} - ${cur_ver}"
    fi

    if [[ ${FIRST_ARG} == "check-updates" ]]
    then
        echo "Exiting..."
        exit 0
    fi
}

execute () {
    java -jar ${PADDLE_HOME}/${APP_NAME}.jar "$@"
}

main () {
    check_prerequisites
    check_config
    check_for_updates
    execute "$@"
}

main "$@"

exit 0
