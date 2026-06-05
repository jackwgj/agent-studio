/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

CREATE TABLE IF NOT EXISTS `t_analytics_event` (
    `event_id`      VARCHAR(64)     NOT NULL COMMENT '事件id， 主键',
    `event_type`    VARCHAR(64)     NOT NULL COMMENT '事件类型',
    `user_id`       VARCHAR(255)    NULL COMMENT 'user id',
    `project_id`    VARCHAR(64)     NULL COMMENT 'project id',
    `app_type`      VARCHAR(255)    NULL COMMENT '应用类型',
    `app_id`        VARCHAR(128)   NULL COMMENT '应用id',
    `channel`       VARCHAR(255)    NULL COMMENT '应用发布渠道',
    `event_time`    TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    `event_date`    TIMESTAMP       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件日期',
    `new_app_user`  TINYINT(1)      NULL DEFAULT 0,
    PRIMARY KEY (`event_id`),
    INDEX `event_time`(`event_time` ASC) USING BTREE,
    INDEX `event_date`(`event_date` ASC) USING BTREE,
    INDEX `app_id`(`app_id` ASC) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='分析事件记录表';

CREATE TABLE IF NOT EXISTS `t_task` (
    `id`                VARCHAR(64)     NOT NULL COMMENT '任务 id， 主键',
    `name`              VARCHAR(64)     NOT NULL COMMENT '任务名称，可修改',
    `conversation_id`   VARCHAR(64)     NULL COMMENT '会话ID',
    `user_id`           VARCHAR(64)     NULL COMMENT 'user id',
    `project_id`        VARCHAR(64)     NULL COMMENT 'project id',
    `domain_id`         VARCHAR(64)     NULL COMMENT 'domain id',
    `workspace_id`      VARCHAR(64)     NULL COMMENT 'workspace id',
    `type`              VARCHAR(32)     NULL COMMENT '任务类型',
    `mode`              VARCHAR(32)     NULL COMMENT '任务模式，当前仅支持ASYNC',
    `app_id`            VARCHAR(64)     NULL COMMENT '对应工作流或agent id',
    `app_version`       VARCHAR(128)    NULL COMMENT '任务应用版本',
    `is_published`      TINYINT(1)      NULL COMMENT '此工作流是否是发布后',
    `status`            VARCHAR(32)     NULL COMMENT '任务状态',
    `inputs`            MEDIUMTEXT      NULL COMMENT '任务输入',
    `outputs`           MEDIUMTEXT      NULL COMMENT '任务输出',
    `timeout`           INTEGER         NULL COMMENT '工作流超时时间',
    `message`           VARCHAR(2048)   NULL COMMENT '提示消息',
    `create_time`       TIMESTAMP       NOT NULL COMMENT '创建时间',
    `start_time`        TIMESTAMP       NULL COMMENT '任务实际开始时间',
    `update_time`       TIMESTAMP       NULL COMMENT '任务更新日期',
    `finish_time`       TIMESTAMP       NULL COMMENT '任务结束日期',
    PRIMARY KEY (`id`),
    INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX `idx_status_time`(`status` ASC, `create_time` ASC) USING BTREE,
    INDEX `idx_finish_time`(`finish_time` ASC) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='任务记录表';





