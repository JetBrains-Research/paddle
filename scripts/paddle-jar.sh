#!/usr/bin/env bash

APP_NAME=Paddle

LATEST_BUILD_INFO=$(curl -s https://api.github.com/repos/JetBrains-Research/paddle/releases/latest)
CHANGELOG_URL='https://github.com/JetBrains-Research/paddle/blob/master/CHANGELOG.md'
PADDLE_SHARE=$HOME/.local/share/paddle
FIRST_ARG=$1

check_prerequisites() {
  local required_utils=$1

  for util in ${required_utils}; do
    which "${util}" >/dev/null
    if [[ $? -eq 1 ]]; then
      echo "\"$util\" has to be installed to use the wrapper"
      exit 1
    fi
  done
}

check_config() {
  [[ ! -d ${PADDLE_SHARE} ]] && mkdir -p "${PADDLE_SHARE}"
  [[ ! -f ${PADDLE_SHARE}/version ]] && touch "${PADDLE_SHARE}/version"
}

check_for_updates() {
  local cur_ver
  cur_ver=$(cat "${PADDLE_SHARE}"/version)

  local latest_ver
  latest_ver=$(echo "${LATEST_BUILD_INFO}" | jq -r '.name')

  local bin_type
  bin_type='jar'

  local bin_name
  bin_name="${PADDLE_SHARE}/${APP_NAME}.jar"

  if [[ ${latest_ver} == "" || ${latest_ver} == "null" ]]; then
    echo "WARNING: Couldn't connect to GitHub to check for updates."
    return
  fi

  if [[ ${cur_ver} == "" || ${cur_ver} != "${latest_ver}" || ! -f ${bin_name} || (${FIRST_ARG} == "check-updates" && ${cur_ver} != "${latest_ver}") ]]; then
    if [[ ${latest_ver} == $(cat "${PADDLE_SHARE}/skip-update" 2>/dev/null) && ${FIRST_ARG} != "check-updates" ]]; then
      echo "You are using ${APP_NAME} ${cur_ver}, newer version ($(cat "${PADDLE_SHARE}/skip-update")) is available but was skipped."
      returnf
    fi

    echo "New version of ${APP_NAME} is available - ${latest_ver}"
    echo "CHANGELOG: $CHANGELOG_URL"
    echo "Would you like to update? (yes/no):"

    read -r confirmation
    if [[ ${confirmation} == "yes" ]]; then
      echo "Updating..."
      local url_to_use
      local urls=( $(echo "${LATEST_BUILD_INFO}" | jq -r '.assets[].browser_download_url') )
      for el in "${urls[@]}"; do
        if [[ "$el" =~ .*${bin_type} ]]; then
          url_to_use=$el
        fi
      done
      curl "$url_to_use" -L -s -o "${bin_name}" && echo "${latest_ver}" > "${PADDLE_SHARE}/version"
      echo "Updated!"
    else
      echo "${latest_ver}" >"${PADDLE_SHARE}/skip-update"
      echo "Update to ${latest_ver} skipped."
      echo "You can always check for updates by running \"${APP_NAME} check-updates\""
    fi
  else
    echo "You are using latest version of ${APP_NAME} - ${cur_ver}"
  fi

  if [[ ${FIRST_ARG} == "check-updates" ]]; then
    echo "Exiting..."
    exit 0
  fi
}

execute() {
  check_prerequisites "java"
  java -jar "${PADDLE_SHARE}/${APP_NAME}.jar" "$@"
}

main() {
  check_prerequisites "jq curl"
  check_config
  check_for_updates
  execute "$@"
}

main "$@"

exit 0
