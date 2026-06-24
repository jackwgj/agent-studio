#!/bin/bash
#
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
#
#

umask 0027

SHELL_DIR=$(cd `dirname $0`; pwd)

function after_start(){
  if [[ "${open_permissions}" == "true" ]]; then
    echo "---> change logs permission and umask"
    chmod -R 755 "${LOGGING_LOG_PATH}"
    umask 0022
  fi

  nohup bash -c "source ${SHELL_DIR}/cron_job.sh && start_cron_job" &
}

function main() {
  chmod 750 ${SERVICE_HOME}

  bash ${SHELL_DIR}/start_server.sh

  after_start
}



main

while [ 1 ]; do
  sleep 30
done