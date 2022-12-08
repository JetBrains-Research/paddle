#!/usr/bin/env bash

set -e

BIN_PATH=$HOME/.local/bin
BIN_NAME=paddle
WRAPPER_URL='https://raw.githubusercontent.com/JetBrains-Research/paddle/master/scripts/paddle.sh'

download_wrapper() {
  mkdir -p "${BIN_PATH}"
  curl -s ${WRAPPER_URL} -o "${BIN_PATH}/${BIN_NAME}"
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
}

main() {
  download_wrapper
  check_path
}

main

exit 0
