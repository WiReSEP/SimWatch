#!/bin/sh
if [ -z "$1" ] 
then
  >&2 echo "Usage: $0 path/to/venv"
  exit 1
fi

VENV_PATH=$(readlink -f "$1")
if [ ! -f "$VENV_PATH/bin/activate_this.py" ] 
then
  >&2 echo "Error: '$VENV_PATH' does not contain a valid virtual env"
  exit 1
fi

SCRIPT_PATH=$(readlink -f "$0")
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")

sed -e "s|{{VENV_PATH}}|${VENV_PATH}|g" "${SCRIPT_DIR}/simwatch.fcgi.tmpl" > "${SCRIPT_DIR}/simwatch.fcgi" \
 && echo "Successfuly installed Fast-CGI launch script: ${SCRIPT_DIR}/simwatch.fcgi"
