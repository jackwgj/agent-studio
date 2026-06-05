/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

INSERT  INTO t_tool (tool_id, project_id, workspace_id, tool_display_name, tool_desc, icon, request_info, auth_info,
                      input_schema, output_schema, type, metadata, creator, creator_id, auth_required, is_free)
VALUES ('reserve_meeting_room_9', 'test_project_id', 'default', '预定会议室工具1',
        '预定会议室1，请在需要预定会议室时调用此工具，预定前需要先查询会议室状态', 'data:image/png;base64,123',
        '{"url":"https://fake.com","method":"POST","headers":{"additionalProp1":"string","additionalProp2":"string","additionalProp3":"string"}}',
        '{"scope":"SERVICE","domain":"QUERY","auth_keys":[{"target_name":"X-API-Key","source_name":"X-Key-Source"}],"iam_credentials":{"projectId":"mock_project_id","domainId":"mock_domain_id"}}',
        '{"type":"object","properties":{"start":{"type":"string","location":"Body","name_cn":"开始时间","description":"会议开始时间，格式为yyyy-MM-dd HH:mm","validated":true,"validate_type":"DATE","validate_rule":"xxxx"},"end":{"type":"string","location":"Body","name_cn":"结束时间","description":"会议结束时间，格式为yyyy-MM-dd HH:mm"},"meetingRoom":{"type":"object","location":"Body","name_cn":"会议室","description":"会议室","properties":{"num":{"type":"integer","location":"Body","name_cn":"会议室编号","description":"会议室编号","default":"123"},"isFree":{"type":"boolean","location":"Body","name_cn":"会议室状态","description":"会议室是否空闲","default":"true"}}}},"required":["start","end","meetingRoom"]}',
        '{"type":"object","properties":{"result":{"type":"string","description":"会议室状态"},"seats":{"type":"array","description":"座位编码","items":{"type":"number"}},"computers":{"type":"array","description":"电脑数组","items":{"type":"object","properties":{"name":{"type":"string","description":"电脑名"}},"required":["name"]}}},"required":["result"]}',
        'inner', '{"url":"https://host/v1/api","authType":"OAuth"}', 'test_creator', 'test_creator_id',1,1);