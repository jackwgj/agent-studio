/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema,  type, intf_type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room', 'test_project_id', 'default', 'reserve_meeting_room',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema,  type, intf_type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room_change', 'test_project_id', 'default', 'reserve_meeting_room_change',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id_change');

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema,  type, intf_type, metadata, creator, creator_id)
VALUES ('reserve_meeting_room_service', 'test_project_id', 'default', 'reserve_meeting_room_service',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"SERVICE","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","auth_key":"123test"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema,  type, intf_type, metadata, creator, creator_id)
VALUES ('workflow_test_tool', 'test_project_id', 'default', 'workflow_test_tool',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"IAM","iam_credentials":{"projectId":"fake_project_id","domainId":"fake_domain_id"}}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, intf_type, metadata, creator, creator_id)
VALUES ('workflow_test_stream_tool', 'test_project_id', 'default', 'workflow_test_stream_tool',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"SERVICE","domain":"QUERY","auth_keys":[{"target_name":"X-API-Key","auth_key":"123test"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'custom', 'streaming','{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                         input_schema, output_schema, type, intf_type, metadata, creator, creator_id, is_input_list, is_output_list, test_status)
VALUES ('workflow_no_auth_tool', 'test_project_id', 'default', 'workflow_no_auth_tool',
         '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态',
         'data:image/png;base64,123',
         '{"basic_info":{"host":"ww.asda","path":"/a","protocol":"https"},"tool_info":[{"tool_id":"76a94adcb96f4c7f","tool_display_name":"qweqwe","tool_chinese_name":"sdasdqq","tool_desc":"qweqwe","method":"GET","path":"/aa/{test}"}]}',
         '{"scope":"SERVICE","domain":"HEADERS","auth_keys":[{"target_name":"apikey","auth_key":"fakekey"}]}',
         '[{"tool_id":"76a94adcb96f4c7f","input_schema":"{\"type\":\"object\",\"properties\":{\"aaa\":{\"default\":\"testtest\",\"location\":\"Headers\",\"validate_rule\":\"\",\"validate_type\":\"CHAR\",\"validated\":false,\"name_cn\":\"\",\"type\":\"string\",\"description\":\"aaa\"},\"aaaa\":{\"default\":\"testtest\",\"location\":\"Body\",\"validate_rule\":\"\",\"validate_type\":\"CHAR\",\"validated\":false,\"name_cn\":\"\",\"type\":\"string\",\"description\":\"tee\"},\"asdqwe\":{\"location\":\"Query\",\"validate_rule\":\"\",\"validate_type\":\"CHAR\",\"validated\":false,\"name_cn\":\"\",\"type\":\"string\",\"description\":\"qwe\"},\"test\":{\"location\":\"Path\",\"validate_rule\":\"\",\"validate_type\":\"CHAR\",\"validated\":false,\"name_cn\":\"\",\"type\":\"string\",\"description\":\"test\"}},\"required\":[\"aaa\",\"aaaa\",\"asdqwe\",\"test\"]}"}]',
         '[{"tool_id":"76a94adcb96f4c7f","output_schema":"{\"type\":\"object\",\"properties\":{\"content\":{\"type\":\"string\",\"description\":\"\"}},\"required\":[\"content\"]}"}]',
         'custom', '[{"tool_id":"36a1a21c18424805","intf_type":"blocking"}]', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id','[{"tool_id":"76a94adcb96f4c7f","is_input_list":0}]','[{"tool_id":"76a94adcb96f4c7f","is_output_list":0}]','[{"tool_id":"76a94adcb96f4c7f","test_status":2}]');


INSERT  INTO t_tool
(tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
 input_schema, output_schema, type, intf_type, metadata, creator, creator_id)
VALUES
('inner_tool', 'test_op_project_id', 'default', 'inner_tool',
 '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态',
 'data:image/png;base64,123',
 '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
 '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
 '',
 '',
 'inner', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id');


INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                     input_schema, output_schema,  type, intf_type, metadata, creator, creator_id, auth_required)
VALUES ('reserve_meeting_room2', 'test_project_id', 'default', 'reserve_meeting_room',
        '预定会议室，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"basic_info": {"protocol": "https","host": "fake.com","base_path": ""}, "tools_info": [{"tool_index": 1,"tool_info": {"path": "","method": "POST"}}]}',
        '{"scope":"USER","domain":"HEADERS","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}]}',
        '{"type":"object","properties":{"start":{"type":"string","description":"会议开始时间，格式为yyyy-MM-dd HH:mm"},"end":{"type":"string","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"string","description":"会议室"}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"}}}',
        'inner', 'streaming', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id',1);
