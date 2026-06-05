/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('workflow_test_tool', 'test_project_id', 'default', 'workflow_test_tool',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST"}',
        '{"scope":"IAM"}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');