/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
INSERT
 INTO t_tool (tool_id, project_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id, visibility)
VALUES ('preset_OCR', 'test_project_id', 'test1', 'xxx', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"xxx"},"end":{"type":"string","description":"xxx"},"meetingRoom":{"type":"string","description":"xx"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"xxx"}}}', 'inner', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id', 'project');

INSERT
 INTO t_tool (tool_id, project_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('preset_Read_File', 'test_project_id', 'test2', 'xxx', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"xxx"},"end":{"type":"string","description":"xxx"},"meetingRoom":{"type":"string","description":"xx"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"xxx"}}}', 'inner', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT
 INTO t_tool (tool_id, project_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id)
VALUES ('preset_Read_Document', 'test_project_id', 'test3', 'xxx', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"xxx"},"end":{"type":"string","description":"xxx"},"meetingRoom":{"type":"string","description":"xx"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"xxx"}}}', 'inner', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');