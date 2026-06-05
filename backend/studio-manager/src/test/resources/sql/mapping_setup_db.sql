/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT
 INTO t_mapping(mapping_id, app_id, app_type, app_name, resource_id, resource_type, resource_name, created_on, updated_on, extends)
VALUES ('test_mapping_id', 'test_agent_id', '测试Agent', 'test_agent', 'test_code_interpreter_workflow_id', 'workflow', '代码执行', '2024-08-12 20:34:00', '2024-08-12 20:34:00', null);

INSERT
 INTO t_mapping(mapping_id, app_id, app_type, app_name, resource_id, resource_type, resource_name, created_on, updated_on, extends)
VALUES ('test_mapping_id9', 'test_agent_id', '测试Agent', 'test_agent', 'reserve_meeting_room_9', 'tool', '预定会议室工具1', '2024-08-12 20:34:00', '2024-08-12 20:34:00', null);

INSERT
 INTO t_mapping(mapping_id, app_id, app_type, app_name, resource_id, resource_type, resource_name, valid, created_on, updated_on, extends)
VALUES ('test_mapping_id10', 'test_agent_id', '测试Agent', 'test_agent', '276a2cd7b4b646f793d7d1da8ae4bf54', 'mcp', 'mcp_server', 1, '2024-08-12 20:34:00', '2024-08-12 20:34:00', null);
