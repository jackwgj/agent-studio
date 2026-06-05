/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

CREATE TABLE IF NOT EXISTS t_analytics_event
(
    event_id     VARCHAR(64)  NOT NULL PRIMARY KEY,
    event_type   VARCHAR(64)  NOT NULL,
    user_id      VARCHAR(255) NULL,
    project_id   VARCHAR(64)  NULL,
    app_type     VARCHAR(255) NULL,
    app_id       VARCHAR(128) NULL,
    channel      VARCHAR(255) NULL,
    event_time   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    event_date   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    new_app_user SMALLINT DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_t_analytics_event_event_time ON t_analytics_event (event_time ASC);
CREATE INDEX IF NOT EXISTS idx_t_analytics_event_event_date ON t_analytics_event (event_date ASC);
CREATE INDEX IF NOT EXISTS idx_t_analytics_event_app_id ON t_analytics_event (app_id ASC);

COMMENT ON TABLE t_analytics_event IS '分析事件记录表';
COMMENT ON COLUMN t_analytics_event.event_id IS '事件id，主键';
COMMENT ON COLUMN t_analytics_event.event_type IS '事件类型';
COMMENT ON COLUMN t_analytics_event.user_id IS 'user id';
COMMENT ON COLUMN t_analytics_event.project_id IS 'project id';
COMMENT ON COLUMN t_analytics_event.app_type IS '应用类型';
COMMENT ON COLUMN t_analytics_event.app_id IS '应用id';
COMMENT ON COLUMN t_analytics_event.channel IS '应用发布渠道';
COMMENT ON COLUMN t_analytics_event.event_time IS '事件时间';
COMMENT ON COLUMN t_analytics_event.event_date IS '事件日期';
COMMENT ON COLUMN t_analytics_event.new_app_user IS '新应用用户';

CREATE TABLE IF NOT EXISTS t_task
(
    id              VARCHAR(64) NOT NULL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    conversation_id VARCHAR(64),
    user_id         VARCHAR(64),
    project_id      VARCHAR(64),
    domain_id       VARCHAR(64),
    workspace_id    VARCHAR(64),
    type            VARCHAR(32),
    mode            VARCHAR(32),
    app_id          VARCHAR(64),
    app_version     VARCHAR(128),
    is_published    BOOLEAN DEFAULT FALSE,
    status          VARCHAR(32),
    inputs          TEXT,
    outputs         TEXT,
    timeout         INTEGER,
    message         VARCHAR(2048),
    create_time     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    start_time      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    finish_time     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_task IS '任务记录表';
COMMENT ON COLUMN t_task.id IS '任务 id，主键';
COMMENT ON COLUMN t_task.name IS '任务名称，可修改';
COMMENT ON COLUMN t_task.conversation_id IS '会话ID';
COMMENT ON COLUMN t_task.user_id IS 'user id';
COMMENT ON COLUMN t_task.project_id IS 'project id';
COMMENT ON COLUMN t_task.domain_id IS 'domain id';
COMMENT ON COLUMN t_task.workspace_id IS 'workspace id';
COMMENT ON COLUMN t_task.type IS '任务类型';
COMMENT ON COLUMN t_task.mode IS '任务模式，当前仅支持ASYNC';
COMMENT ON COLUMN t_task.app_id IS '对应工作流或agent id';
COMMENT ON COLUMN t_task.app_version IS '任务应用版本';
COMMENT ON COLUMN t_task.is_published IS '此工作流是否是发布后';
COMMENT ON COLUMN t_task.status IS '任务状态';
COMMENT ON COLUMN t_task.inputs IS '任务输入';
COMMENT ON COLUMN t_task.outputs IS '任务输出';
COMMENT ON COLUMN t_task.timeout IS '工作流超时时间';
COMMENT ON COLUMN t_task.message IS '提示消息';
COMMENT ON COLUMN t_task.create_time IS '创建时间';
COMMENT ON COLUMN t_task.start_time IS '任务实际开始时间';
COMMENT ON COLUMN t_task.update_time IS '任务更新日期';
COMMENT ON COLUMN t_task.finish_time IS '任务结束日期';

CREATE INDEX IF NOT EXISTS idx_create_time ON t_task (create_time ASC);
CREATE INDEX IF NOT EXISTS idx_status_time ON t_task (status ASC, create_time ASC);
CREATE INDEX IF NOT EXISTS idx_finish_time ON t_task (finish_time ASC);