#!/bin/bash

SCRIPT_PATH=$(readlink -f "$0")
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")
SCRIPT_NAME=$(basename "${BASH_SOURCE[0]}")
FUNCTION_NAME_BACKUP_COMMON_LOG=backup_common_log

echo $SCRIPT_PATH $SCRIPT_DIR $SCRIPT_NAME

function start_cron_job() {
    if [[ "${BACKUP_COMMON_LOG:-true}" != "true" ]]; then
        return 0
    fi
    sleep 10

    # 启动后1分钟开始定时任务
    sleep ${WATCH_DOG_INIT_TIME:-60}

    PERIOD_HOUR=${BACKUP_COMMON_LOG_PERIOD_HOUR:-4}

    # 容器初始没有定时任务，不用缓存已有的定时任务
    # 滚动开关
    if [[ "${BACKUP_COMMON_LOG:-true}" == "true" ]]; then
      echo "BACKUP_COMMON_LOG_PERIOD_HOUR=${BACKUP_COMMON_LOG_PERIOD_HOUR}" > /tmp/cron_jobs
      echo "BACKUP_COMMON_LOG_BACKUP_NUM=${BACKUP_COMMON_LOG_BACKUP_NUM}" >> /tmp/cron_jobs
      echo "0 */${PERIOD_HOUR} * * * bash -c 'cd ${SCRIPT_DIR} && source ${SCRIPT_DIR}/${SCRIPT_NAME} && ${FUNCTION_NAME_BACKUP_COMMON_LOG}'" >> /tmp/cron_jobs
    fi
    crontab /tmp/cron_jobs
    echo "current crontab list:"
    crontab -l
}


function backup_common_log() {
    backup_server_log
}

function backup_server_log() {
    BACKUP_PATH="/opt/cloud/logs/common_log_backup"
    COMMON_LOG_FILE="/opt/cloud/logs/common.log"
    BACKUP_NUM=${BACKUP_COMMON_LOG_BACKUP_NUM:-10}

    # 仅判断是否为空，行里的函数无法获取文件信息
    if [ -z "$(tail -n 1 ${COMMON_LOG_FILE})" ]; then
        # 文件为空，不滚动
        return 0
    fi

    mkdir -p $BACKUP_PATH
    # 统计目录中的文件数量（仅统计普通文件）
    file_count=$(find "$BACKUP_PATH" -maxdepth 1 -type f | wc -l)

    if [ "$file_count" -ge $BACKUP_NUM ]; then
        # 找到最旧的文件（按修改时间排序，最旧的在前）
        oldest_file=$(find $BACKUP_PATH -maxdepth 1 -type f -printf '%T@ %p\n' | sort -rnk 1 | tail -n 1 | awk '{print $2}')

        if [ -n "$oldest_file" ]; then
            echo "Removing oldest file: $oldest_file"
            rm -f "$oldest_file"
        fi
    fi

    cp ${COMMON_LOG_FILE} "${BACKUP_PATH}/common.log.$(date +%Y%m%d_%H%M)00"
    echo "" > ${COMMON_LOG_FILE}
}

