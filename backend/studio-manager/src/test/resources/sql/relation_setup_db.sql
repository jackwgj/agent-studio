
INSERT INTO `t_tool` (`tool_id`, `project_id`, `tool_display_name`, `tool_chinese_name`, `tool_desc`, `icon`, `icon_name`, `visibility`, `request_info`, `auth_info`, `input_schema`, `output_schema`, `intf_type`, `type`, `metadata`, `creator`, `creator_id`, `created_on`, `updated_on`, `test_status`, `last_version_id`, `customize_node`, `is_input_list`, `is_output_list`, `auth_required`, `workspace_id`, `trace_id`, `published`, `call_mode`, `is_free`, `domain_id`)
VALUES ('relation_setup_db_resource1', 'test_project_id', 'mage_translate_1', 'mage_translate_1', '文本翻译工具，主要将输入中文转成英文', '', NULL, 'project', '{"basic_info":{"host":"127.0.0.1:4878","path":"","protocol":"http"},"tool_info":[{"tool_id":"0","tool_display_name":"mage_translate_1","tool_chinese_name":"mage_translate_1","tool_desc":"文本翻译工具，主要将输入中文转成英文","url":"http://127.0.0.1:4878/translate","method":"POST","headers":{"Content-Type":"application/json"},"path":"/translate"}]}', '{}', '[{"tool_id":"0","input_schema":"{\"type\":\"object\",\"properties\":{\"text\":{\"location\":\"Body\",\"validate_rule\":\"\",\"validate_type\":\"CHAR\",\"validated\":false,\"name_cn\":\"待翻译的文本\",\"type\":\"string\",\"description\":\"待翻译的文本，标点符号都是中文标点符号\"}},\"required\":[\"text\"]}"}]', '[{"tool_id":"0","output_schema":"{\"type\":\"object\",\"properties\":{\"response_data\":{\"type\":\"string\",\"description\":\"返回数据\"}},\"required\":[\"response_data\"]}"}]', '[{"tool_id":"0","intf_type":"blocking"}]', 'custom', NULL, '官方预置', '6f80301edacc43728bf47b6a6484d512', '2025-09-09 20:58:20', '2025-11-15 14:17:41', '[{"tool_id":"0","test_resource_status":2}]', '1762262594882', NULL, '[{"tool_id":"0","is_input_list":false}]', '[{"tool_id":"0","is_output_list":false}]', 0, 'default', NULL, 1, 'api', 0, NULL);

INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_1', 'relation_setup_db_app1', '1', 'agent',
        'relation_setup_db_resource1', 'tool', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app1', 'relation_setup_db_resource1',
        NULL, NULL,
        1);

INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_2', 'relation_setup_db_app2', NULL, 'workflow',
        'relation_setup_db_resource1', 'tool', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app2', 'relation_setup_db_resource1',
        NULL, NULL,
        1);

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('relation_setup_db_resource3', 'questions测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_resource_user_id', 'jujiawei', 'test_resource_user_id', 'test_project_id', 'test_resource_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_resource_questions_workflow_id.json', 'task');
INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                        resource_type, created_on, updated_on, app_name, resource_name,
                        resource_version, resource_desc, valid)
VALUES ('relation_setup_db_9', 'relation_setup_db_app9', NULL, 'agent',
        'relation_setup_db_resource3', 'workflow', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app9', 'relation_setup_db_resource9',
        NULL, NULL,
        1);

INSERT  INTO t_agent(agent_id, project_id, workspace_id,  name, description, icon, instructions, trigger_list, prologue, suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on, published_on, model_config, deleted)
VALUES ('relation_setup_db_resource4', 'test_project_id', 'default', '测试Agent1111111111111111111111111111111111111111111111111111111', '测试Agent描述', 'data:image/png;base64,123', NULL, '[{"trigger_id":"test_resource_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', NULL, NULL, '{"enable":true,"rounds":1,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}', 'published', 'test_resource_creator', 'test_resource_creator_id', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '{"top_p":0.5,"temperature":0.5}', 0);

INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                        resource_type, created_on, updated_on, app_name, resource_name,
                        resource_version, resource_desc, valid)
VALUES ('relation_setup_db_10', 'relation_setup_db_app10', NULL, 'workflow',
        'relation_setup_db_resource4', 'agent', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app10', 'relation_setup_db_resource10',
        NULL, NULL,
        1);

INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_3', 'relation_setup_db_app1', '1', 'agent',
        'relation_setup_db_resource2', 'tool', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app1', 'relation_setup_db_resource2',
        NULL, NULL,
        1);

INSERT  INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status,
                                 creator, creator_id, released_on, extend1, dsl_path, ir_path)
VALUES ('relation_setup_db_4', '123', '123', 'test',
        'relation_setup_db_resource1', 'tool',
        'normal', '2', '2', '2025-03-03 15:36:54', '', '', '');

INSERT  INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status,
                                        creator, creator_id, released_on, extend1, dsl_path, ir_path)
VALUES ('relation_setup_db_5', '123', '123', 'test',
        'relation_setup_db_resource2', 'tool',
        'normal', '2', '2', '2025-03-03 15:36:54', '', '', '');

INSERT  INTO t_agent(agent_id, project_id, workspace_id,  name, description, icon, instructions, trigger_list, prologue, suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on, published_on, model_config, deleted)
VALUES ('test_agent_id_0818001', 'test_project_id', 'default', '测试Agent1111111111111111111111111111111111111111111111111111111', '测试Agent描述', 'data:image/png;base64,123', NULL, '[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', NULL, NULL, '{"enable":true,"rounds":1,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}', 'published', 'test_creator', 'test_creator_id', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '{"top_p":0.5,"temperature":0.5}', 0);

INSERT  INTO t_agent(agent_id, project_id, workspace_id,  name, description, icon, instructions, trigger_list, prologue, suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on, published_on, model_config, deleted)
VALUES ('relation_setup_db_app1', 'test_project_id', 'default', '测试Agent1111111111111111111111111111111111111111111111111111111', '测试Agent描述', 'data:image/png;base64,123', NULL, '[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', NULL, NULL, '{"enable":true,"rounds":1,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}', 'published', 'test_creator', 'test_creator_id', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '{"top_p":0.5,"temperature":0.5}', 0);

INSERT  INTO t_agent(agent_id, project_id, workspace_id,  name, description, icon, instructions, trigger_list, prologue, suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on, published_on, model_config, deleted)
VALUES ('relation_setup_db_app7', 'test_project_id', 'default', '测试Agent1111111111111111111111111111111111111111111111111111111', '测试Agent描述', 'data:image/png;base64,123', NULL, '[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', NULL, NULL, '{"enable":true,"rounds":1,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}', 'published', 'test_creator', 'test_creator_id', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '{"top_p":0.5,"temperature":0.5}', 0);

INSERT  INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id, fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id, dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id, readme, server_config, tools, deploy_type, org_type, function_name, server_id, function_urn, function_application_name, domain_id, unique_code, project_id, workspace_id, icon) VALUES ('276a2cd7b4b646f793d7d1da8ae4bf54', '测试8', 'Empty Template', '基于空白模板快速构建自己的业务逻辑', 'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system', '2025-08-18 09:56:16', '2', '2025-08-18 14:24:51', '2', null, '"mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8000/sse"
    }
  }', '[{"name":"add","description":"","inputSchema":{"type":"object","properties":{"a":{"title":"A","type":"integer"},"b":{"title":"B","type":"integer"}},"required":["a","b"]}},{"name":"sub1","description":"","inputSchema":{"type":"object","properties":{"a":{"title":"A","type":"integer"},"b":{"title":"B","type":"integer"}},"required":["a","b"]}}]', 'sse', 'SSE', null, '1', null, '测试8-piz7Hngo-AIAE', null, '46ee3000571e4019a2d1039187aa0295', 'test_project_id', 'default', null);
INSERT  INTO t_mapping (mapping_id, app_id, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_6', 'test_agent_id_0818001', 'agent',
        '276a2cd7b4b646f793d7d1da8ae4bf54', 'mcp', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app1', 'relation_setup_db_resource2',
        NULL, NULL,
        1);

INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_7', 'relation_setup_db_app7', 7, 'workflow',
        'relation_setup_db_resource7', 'model', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app7', 'relation_setup_db_resource7',
        NULL, NULL,
        1);
INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                                resource_type, created_on, updated_on, app_name, resource_name,
                                resource_version, resource_desc, valid)
VALUES ('relation_setup_db_8', 'relation_setup_db_app7', 7, 'workflow',
        'relation_setup_db_resource77', 'workflow', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app7', 'relation_setup_db_resource77',
        NULL, NULL,
        1);
INSERT
INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('relation_setup_db_app7', 'questions测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_resource_user_id', 'jujiawei', 'test_resource_user_id', 'test_project_id', 'test_resource_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_resource_questions_workflow_id.json', 'task');

INSERT INTO `t_skill` (`skill_id`, `domain_id`, `name`, `icon`, `status`, `source`, `description`, `creator_id`,
                       `creator_name`, `created_at`, `updated_at`, `latest_version`, `used_version`)
VALUES ('test_skill_id', '0c72472bfe4647168124456891091495', 'test_agent_skill',
        'bdf8e247-9ff8-4b1f-9148-040b7c288894.png', 'deploy', 'custom', '123123', '6f80301edacc43728bf47b6a6484d512',
        'test', '1774530861889', '1774530861889', '111',
        'test_used_version');
INSERT  INTO t_mapping (mapping_id, app_id, app_version, app_type, resource_id,
                        resource_type, created_on, updated_on, app_name, resource_name,
                        resource_version, resource_desc, valid)
VALUES ('relation_setup_db_11', 'relation_setup_db_app8', 7, 'agent',
        'test_skill_id', 'skill', '2025-07-07 16:22:17', '2025-07-07 16:22:17',
        'relation_setup_db_app8', 'test_agent_skill',
        'test_used_version', NULL,
        1);

INSERT INTO t_agent(agent_id, project_id, workspace_id, name, description, icon, instructions, trigger_list, prologue,
                    suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on,
                    published_on, model_config, deleted)
VALUES ('relation_setup_db_app8', 'test_project_id', 'default',
        '测试Agent1111111111111111111111111111111111111111111111111111111', '测试Agent描述',
        'data:image/png;base64,123', NULL,
        '[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]',
        NULL, NULL,
        '{"enable":true,"rounds":1,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}',
        'published', 'test_creator', 'test_creator_id', '2024-08-12 20:34:00', '2024-08-12 20:34:00',
        '2024-08-12 20:34:00', '{"top_p":0.5,"temperature":0.5}', 0);

