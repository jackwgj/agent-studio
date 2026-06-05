/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT
 INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id, visibility)
VALUES ('reserve_meeting_room_1', 'test_project_id', 'default', '预定会议室工具1',
        '预定会议室1，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}', 'custom', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id', 'project');

INSERT
 INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room_2', 'test_project_id', 'default', '预定会议室工具2',
        '预定会议室2，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}', 'custom', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT
 INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room_3', 'test_project_id', 'default', '预定会议室工具3',
        '预定会议室2，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}', 'inner', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT
 INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room_9', 'test_project_id', 'default', '预定会议室工具9',
        '预定会议室2，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}', 'inner',
        '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');