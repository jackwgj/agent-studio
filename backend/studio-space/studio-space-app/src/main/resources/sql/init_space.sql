CREATE TABLE IF NOT EXISTS `t_agent_space_login_log`
(
    `id`           varchar(128) NOT NULL COMMENT '主键',
    `login_domain` varchar(255) COMMENT '登录域名',
    `user_id`      varchar(32) COMMENT '用户sn',
    `oper_ip`      varchar(48)  NOT NULL COMMENT '登录IP地址',
    `oper_type`    varchar(32)  NOT NULL COMMENT '操作类型：登录login，登出logout',
    `session_id`   varchar(128) COMMENT '会话标识，加密存储',
    `service_ip`   varchar(48) COMMENT '登录的服务器ip',
    `oper_time`    datetime COMMENT '登录/登出时间',
    `login_center` varchar(32)  NOT NULL COMMENT '从哪个中心登录',
    `logout_type`  varchar(32) COMMENT '登出类型，包括：会话过期登出，用户主动登出',
    `success_flag` tinyint(1)   NULL DEFAULT 1 COMMENT '0:失败，1:成功',
    `content`      longtext COMMENT '失败等场景记录的原因信息',
    `domain_id`    varchar(32) COMMENT '租户标识',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS `t_agent_space_oper_log`
(
    `id`            bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键，log_id',
    `obj_type`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作对象类型',
    `obj_id_field`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '记录的操作对象ID字段名',
    `obj_id_value`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '记录的操作对象ID值',
    `oper_type`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作类型：create新增，update修改，delete删除',
    `oper_ip`       varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '操作IP',
    `content`       longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '操作内容',
    `oper_user`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '操作人',
    `oper_time`     datetime                                               NOT NULL COMMENT '操作时间',
    `domain_id`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '租户标识',
    `response_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL,
    `response_msg`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
    `success_flag`  tinyint                                                NOT NULL COMMENT '1:成功，0:失败',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1001
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC COMMENT ='agentSpace操作日志表';
ALTER TABLE `t_agent_space_oper_log` MODIFY COLUMN `success_flag` tinyint NOT NULL DEFAULT 0 COMMENT '1:成功，0:失败';
ALTER TABLE `t_agent_space_oper_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE `t_agent_space_oper_log` MODIFY COLUMN `oper_ip` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作IP';

CREATE TABLE IF NOT EXISTS `ws_agent_task_resource`
(
    `id`                varchar(255) NOT NULL COMMENT '主键',
    `agent_id`          varchar(255) NOT NULL COMMENT '智能体Id',
    `task_id`           varchar(255) NOT NULL COMMENT '智能体Id',
    `is_enable`         tinyint(1)   DEFAULT '0' COMMENT 'agent是否可用',
    `status`            varchar(255) DEFAULT NULL,
    `agent_uri`         varchar(255) DEFAULT NULL COMMENT 'agent访问地址',
    `web_uri`           varchar(255) DEFAULT NULL COMMENT 'agent页面访问地址',
    `created_date`      datetime     DEFAULT NULL COMMENT '创建时间',
    `last_updated_date` datetime     DEFAULT NULL COMMENT '更新时间',
    `resource_id`       varchar(36)  DEFAULT NULL COMMENT '容器资源id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='agent任务表';

CREATE TABLE IF NOT EXISTS `ws_agent_builder_action_def`
(
    `id`                      varchar(64) NOT NULL COMMENT '动作id',
    `step_id`                 varchar(64)   DEFAULT NULL COMMENT '关联的步骤id',
    `type`                    varchar(64)   DEFAULT NULL COMMENT '动作类型;step_start、reply、tool_use、pause',
    `tool_type`               varchar(255)  DEFAULT NULL COMMENT '工具操作类型;当type为tool_use时，详细的类型，如：search、file、terminal',
    `content`                 longtext COMMENT '对话中展示内容',
    `display_content`         text COMMENT '侧边栏展示内容',
    `plan_content`            text COMMENT '修改的计划',
    `reasoning_content`       text COMMENT '深度思考时的思考内容',
    `file_list`               varchar(2048) DEFAULT NULL COMMENT '文件id列表',
    `finished`                int           DEFAULT NULL COMMENT '是否结束',
    `domain_id`               varchar(64)   DEFAULT NULL COMMENT '组织id',
    `dept_code`               varchar(64)   DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime      DEFAULT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64)   DEFAULT NULL COMMENT '创建人id',
    `last_updated_date`       datetime      DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64)   DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int           DEFAULT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`),
    KEY `idx_step_id_deleted` (`step_id`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='动作表';
ALTER TABLE `ws_agent_builder_action_def`
    MODIFY COLUMN `created_date` datetime DEFAULT NULL COMMENT '创建时间',
    MODIFY COLUMN `last_updated_date` datetime DEFAULT NULL COMMENT '更新时间';

CREATE TABLE IF NOT EXISTS `ws_agent_builder_file_def`
(
    `id`                      varchar(64) NOT NULL COMMENT 'id',
    `source_id`               varchar(64) COMMENT '来源文件id,针对重复引用的文件',
    `message_id`              varchar(64) COMMENT '消息id',
    `koo_page_id`             varchar(128)         DEFAULT NULL COMMENT 'kooPage存储Id',
    `name`                    varchar(512)         DEFAULT NULL COMMENT '文件名称',
    `task_id`                 varchar(64)          DEFAULT NULL,
    `uri`                     varchar(2048)        DEFAULT NULL COMMENT '文件相对路径',
    `url`                     varchar(2048)        DEFAULT NULL COMMENT 'obs路径',
    `type`                    int                  DEFAULT NULL COMMENT '文件类型;0：用户上传；1：流程生成',
    `content`                 longtext COMMENT '内容',
    `storage_type`            int         NOT NULL DEFAULT '0' COMMENT '存储类型，0：数据库；1：obs',
    `domain_id`               varchar(64)          DEFAULT NULL COMMENT '组织id',
    `dept_code`               varchar(64)          DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime             DEFAULT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64)          DEFAULT NULL COMMENT '创建人id',
    `last_updated_date`       datetime             DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64)          DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int                  DEFAULT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`),
    KEY `idx_domain_id_task_id_deleted` (`domain_id`, `task_id`, `deleted`),
    KEY `idx_task_id_name_deleted` (`task_id`, `deleted`),
    KEY `idx_task_id` (`task_id`),
    KEY `name_and_task_id` (`name`, `task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='agentBuilder任务相关文件表';

ALTER TABLE `ws_agent_builder_file_def` CHANGE COLUMN `massage_id` `message_id` VARCHAR(64) COMMENT '消息id';
ALTER TABLE ws_agent_builder_file_def ADD extra_config text COMMENT '额外信息';

CREATE TABLE IF NOT EXISTS `ws_agent_builder_message_def`
(
    `id`                      varchar(64) NOT NULL COMMENT '消息id',
    `parent_id`               varchar(64) COMMENT '父消息ID，提问的消息id',
    `task_id`                 varchar(64)   DEFAULT NULL COMMENT '关联的任务id',
    `type`                    int           DEFAULT NULL COMMENT '消息类型;1：用户；2：系统',
    `content`                 text COMMENT '消息内容',
    `file_list`               varchar(2048) DEFAULT NULL COMMENT '文件id列表',
    `mcp_list`                varchar(2048) DEFAULT NULL COMMENT 'mcp的id列表',
    `domain_id`               varchar(64)   DEFAULT NULL COMMENT '组织id',
    `dept_code`               varchar(64)   DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime      DEFAULT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64)   DEFAULT NULL COMMENT '创建人id',
    `last_updated_date`       datetime      DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64)   DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int           DEFAULT NULL COMMENT '是否已删除',
    `rating`                  int           DEFAULT (-1) COMMENT '用户评分 1-10 前端展示1-5允许半分',
    PRIMARY KEY (`id`),
    KEY `idx_task_id_deleted` (`task_id`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='消息历史表';
ALTER TABLE `ws_agent_builder_message_def`
    MODIFY COLUMN `created_date` datetime DEFAULT NULL COMMENT '创建时间',
    MODIFY COLUMN `last_updated_date` datetime DEFAULT NULL COMMENT '更新时间';

CREATE TABLE IF NOT EXISTS `ws_agent_builder_quota_def`
(
    `id`                      varchar(64) NOT NULL COMMENT 'id',
    `type`                    varchar(255) DEFAULT NULL COMMENT '资源类型',
    `quota`                   int          DEFAULT NULL COMMENT '配额',
    `used`                    int          DEFAULT NULL COMMENT '使用量',
    `domain_id`               varchar(64)  DEFAULT NULL COMMENT '组织id',
    `dept_code`               varchar(64)  DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime     DEFAULT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64)  DEFAULT NULL COMMENT '创建人id',
    `last_updated_date`       datetime     DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64)  DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int          DEFAULT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='配额表';


CREATE TABLE IF NOT EXISTS `ws_agent_builder_step_def`
(
    `id`                      varchar(64) NOT NULL COMMENT '步骤id',
    `agent_name`              varchar(64) DEFAULT NULL COMMENT 'agent名称',
    `message_id`              varchar(64) DEFAULT NULL COMMENT '关联的消息id',
    `finished`                int         DEFAULT NULL COMMENT '是否已结束',
    `domain_id`               varchar(64) DEFAULT NULL COMMENT '组织id',
    `dept_code`               varchar(64) DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime    DEFAULT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64) DEFAULT NULL COMMENT '创建人id',
    `last_updated_date`       datetime    DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64) DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int         DEFAULT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`),
    KEY `idx_message_id_deleted` (`message_id`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='步骤表;是一个分区分块的概念，具体内容由其下的action承载';
ALTER TABLE `ws_agent_builder_step_def`
    MODIFY COLUMN `created_date` datetime DEFAULT NULL COMMENT '创建时间',
    MODIFY COLUMN `last_updated_date` datetime DEFAULT NULL COMMENT '更新时间';

CREATE TABLE IF NOT EXISTS `ws_agent_builder_task_def`
(
    `id`                      varchar(64)   NOT NULL COMMENT '任务id',
    `name`                    varchar(255)  DEFAULT NULL COMMENT '任务名称',
    `agent_type`              int           DEFAULT NULL COMMENT 'agent类型;1:内置智能体,2:工作流智能体;3单智能体;4多智能体',
    `run_type`                int           DEFAULT 0 NOT NULL COMMENT '会话是由哪个页面发起的;0:AgentSpace,1:AgentWeb;',
    `agent_id`                varchar(64)   NULL COMMENT '对应的agent_id或short_code',
    `agent_list`              varchar(255)  DEFAULT NULL COMMENT 'agent_id列表',
    `run_mode`                int           DEFAULT NULL COMMENT '运行类型;0：规划模式；1：探索模式',
    `status`                  int           DEFAULT NULL COMMENT '0：未开始；1：运行中；2：等待用户输入；3：触发暂停；4：暂停，等待用户输入；5：任务正常结束，等待用户输入；6：任务删除；7：任务完成，寿终正寝；8：运行失败，意外身亡；',
    `display_info`            varchar(2048) DEFAULT NULL COMMENT '任务在前端展示的附加信息;[json结构]is_pin：是否置顶任务（布尔值，false 表示未置顶，true 表示置顶显示）is_dnd：是否开启免打扰（布尔值，false 表示正常通知，true 表示关闭任务通知）',
    `extra_agent_config`      varchar(2048) DEFAULT NULL COMMENT 'agent的额外配置;兼容自定agent场景的额外配置',
    `scheduled`               int           DEFAULT NULL COMMENT '是否为定时任务',
    `storage_type`            int           DEFAULT NULL COMMENT '存储类型;0：DB；1：ES',
    `domain_id`               varchar(64)   NOT NULL COMMENT '组织id',
    `dept_code`               varchar(64)   DEFAULT NULL COMMENT '部门id',
    `created_date`            datetime      NOT NULL COMMENT '创建时间',
    `created_by_user_id`      varchar(64)   NOT NULL COMMENT '创建人id',
    `last_updated_date`       datetime      DEFAULT NULL COMMENT '更新时间',
    `last_updated_by_user_id` varchar(64)   DEFAULT NULL COMMENT '更新人id',
    `deleted`                 int           DEFAULT NULL COMMENT '是否已删除',
    PRIMARY KEY (`id`),
    KEY `idx_domain_id_deleted` (`domain_id`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='任务表';


CREATE TABLE IF NOT EXISTS t_agent_default
(
    id           varchar(64)  not null primary key,
    agent_id     varchar(64)  null,
    name         varchar(128) not null,
    description  varchar(255) null,
    icon         mediumtext   null,
    creator      varchar(64)  null,
    created_date datetime     null,
    updated_at   datetime     null,
    type         int          null,
    sort         int          null comment '排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='系统内置智能体表';


CREATE TABLE IF NOT EXISTS `t_agent`
(
    id                varchar(64)   NOT NULL COMMENT '主键id',
    agent_id          varchar(64)   NOT NULL COMMENT 'agent唯一标识',
    type              varchar(32)   NULL COMMENT 'agent类型',
    workspace_id      varchar(64)   NOT NULL COMMENT '空间id',
    creator           varchar(64)   NULL COMMENT '创建人',
    status            varchar(32)   NULL COMMENT 'agent状态',
    domain_id         varchar(64)   NOT NULL COMMENT '项目唯一标识',
    created_date      datetime      NOT NULL COMMENT '创建时间',
    last_updated_date datetime      NULL COMMENT '更新时间',
    deleted           int DEFAULT 0 NULL COMMENT '是否已删除(0-未删除，1-已删除)',
    PRIMARY KEY (id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_domain_id (domain_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='快捷命令表';


CREATE TABLE IF NOT EXISTS `t_quick_command`
(
    id                      varchar(64) not null comment '主键id',
    agent_id                varchar(64) not null comment '智能体应用id',
    name                    varchar(64) not null comment '快捷指令名称',
    active                  int         not null comment '是否开启关闭，0为关闭，1为开启',
    description             longtext    null     comment '快捷指令描述',
    content                 longtext    not null comment '快捷指令内容',
    display_no              int         not null comment '快捷指令序号',
    recommend               int         not null comment '是否为推荐快捷指令，0为非推荐指令，1为推荐指令',
    domain_id               varchar(64) not null comment '组织id',
    created_date            datetime    not null comment '创建时间',
    created_by_user_id      varchar(64) not null comment '创建人用户id',
    last_updated_date       datetime    not null comment '最后修改时间',
    last_updated_by_user_id varchar(64) not null comment '最后修改人用户id',
    PRIMARY KEY (`id`),
    KEY `t_quick_command_agent_id_index` (`agent_id`),
    KEY `t_quick_command_domain_id_index` (`domain_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS t_i18n_resource
(
    id       bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    i18n_key varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '国际化Key',
    locale   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '语言区域 (如 zh-cn, en-us)',
    message  text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '翻译后的展示文案',
    PRIMARY KEY (id),
    UNIQUE KEY uk_key_locale (i18n_key,locale)
);
