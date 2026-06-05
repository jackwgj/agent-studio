/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

INSERT
 INTO t_agent_datasource(id, project_id, workspace_id, `name`, `type`, `desc`, internet_access, instance_id, instance_name, connection_info, `status`, last_error_message, created_by, creator_id, created_on, updated_by, updater_id, updated_on)
VALUES ('test_workflow_id', 'test_project_id', 'default', '数据源源13', 'MYSQL', '数据源源描述13', 'public', NULL, NULL, '{\"host\":\"127.0.0.1\",\"port\":\"3306\",\"ssl_enabled\":false,\"database_name\":\"lms_agent_manager\",\"user\":\"root\",\"password\":\"asdas\",\"sql_version\":\"5.0.1\"}', 'failed', 'connection to datasource failed!', 'test_user_id', 'test_user_id', '2025-06-23 14:53:11', 'test_user_id', 'test_user_id', '2025-06-23 14:53:11');
