#!/usr/bin/env bash

set -e

BIN_PATH=$HOME/.local/bin
BIN_NAME=paddle
PADDLE_WRAPPER_URL='https://raw.githubusercontent.com/JetBrains-Research/paddle/master/scripts/paddle.sh' # native image wrapper
PADDLE_JAR_WRAPPER_URL='https://raw.githubusercontent.com/JetBrains-Research/paddle/master/scripts/paddle-jar.sh' # jar wrapper
FIRST_ARG=$1

download_wrapper() {
  mkdir -p "${BIN_PATH}"

  if [[ ${FIRST_ARG} == "jar" ]]; then
    curl -s ${PADDLE_JAR_WRAPPER_URL} -o "${BIN_PATH}/${BIN_NAME}"
  else
    curl -s ${PADDLE_WRAPPER_URL} -o "${BIN_PATH}/${BIN_NAME}"
  fi

  chmod +x "${BIN_PATH}/${BIN_NAME}"
}

check_path() {
  if [[ $(echo "$PATH" | grep "${BIN_PATH}") == "" ]]; then
    echo "Looks like $BIN_PATH is not in path. Would you like to set it automatically? (yes/no):"

    read -r confirmation
    [[ ${confirmation} == "yes" ]] && set_path
  fi
}

set_path() {
  local sh_rc_path=''
  local export_path=''

  # shellcheck disable=SC2016
  case $SHELL in
  /bin/bash)
    sh_rc_path=$HOME/.bashrc
    export_path='export PATH="$HOME/.local/bin:$PATH"'
    ;;
  /bin/zsh)
    sh_rc_path=$HOME/.zshrc
    export_path='export PATH="$HOME/.local/bin:$PATH"'
    ;;
  /bin/tcsh)
    sh_rc_path=$HOME/.tcshrc
    export_path='set path = ($path $HOME/.local/bin)'
    ;;
  /bin/kshrc)
    sh_rc_path=$HOME/.kshrc
    export_path='export PATH="$HOME/.local/bin:$PATH"'
    ;;
  /bin/fish)
    sh_rc_path=$HOME/.config/fish/config.fish
    export_path='set -gx PATH $HOME/.local/bin $PATH'
    ;;
  *)
    echo "Unknown shell: $SHELL"
    exit 1
    ;;
  esac

  grep -qxF "${export_path}" "${sh_rc_path}" || echo "${export_path}" >>"${sh_rc_path}"
  echo "Please, reload your shell or run 'source ${sh_rc_path}' to update PATH variable"
}

main() {
  download_wrapper
  check_path
}

main

exit 0
