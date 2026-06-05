/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
INSERT
INTO t_mcp (server_id, project_id, server_name, server_name_en, category, server_desc, icon, tools, visibility, url, auth, type, creator, creator_id, created_on, updated_on)
VALUES ('test_mcp_server_id', 'test_project_id', 'test_server_name', 'test_server_name_en', 'public','desc', 'null', '{"count":1,"tools":[{"name":"calculate","desc":"Calculates/evaluates the given expression.","input":"{\"type\":\"object\",\"properties\":{\"expression\":{\"title\":\"Expression\",\"type\":\"string\"}},\"required\":[\"expression\"]}","output":"{\"type\":\"object\",\"properties\":{\"isError\":{\"type\":\"boolean\"},\"content\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"type\":{\"type\":\"string\"},\"text\":{\"type\":\"string\"}},\"required\":[\"type\",\"text\"],\"additionalProperties\":false}}},\"required\":[\"content\"],\"additionalProperties\":false}","params_count":1}]}', 'project', 'http://localhost:8005/sse','{"scope":"SERVICE","domain":"HEADERS","auth_keys":[{"source_name":"x-auth-token","target_name":"x-auth-token","auth_key":"11"}]}', 'custom', 'test_creator', 'test_creator_id', '2025-04-11 14:25:05', '2025-04-11 14:25:08');

INSERT
INTO t_mcp (server_id, project_id, server_name, server_name_en, server_desc, icon, tools, visibility, url, auth, type, creator, creator_id, created_on, updated_on)
VALUES ('test_mcp_server_id1', 'test_project_id', 'test_server_name1', 'test_server_name_en1', 'desc', 'icon', 'null', 'project', 'http://localhost:8005/sse', '{"scope":"SERVICE","domain":"HEADERS","auth_keys":[{"source_name":"x-auth-token","target_name":"x-auth-token","auth_key":"12345678"}]}', 'custom', 'test_creator', 'test_user_id', '2025-04-11 14:25:05', '2025-04-11 14:25:08');

INSERT  INTO ws_mcp_service_def (id, name, name_en, description, description_en, fc_instance_url, fc_instance_id, fc_instance_status, fc_region, apig_group_id, apig_instance_id, deleted, tenant_id, dept_code, created_date, created_by_user_id, last_updated_date, last_updated_by_user_id, readme, server_config, tools, deploy_type, org_type, function_name, server_id, function_urn, function_application_name, domain_id, unique_code, project_id, workspace_id, icon,visibility) VALUES ('276a2cd7b4b646f793d7d1da8ae4bf54', '测试8', 'Empty Template', '基于空白模板快速构建自己的业务逻辑', 'Base on empty template to create mcp service', '', null, 'success', null, null, null, 0, 'system', 'system', '2025-08-18 09:56:16', '2', '2025-08-18 14:24:51', '2', null, '{
  "mcpServers" : {
    "example-sse" : {
      "url" : "http://xxxx:8000/sse"
    }
  }
}', '[{"name":"add","description":"","inputSchema":{"type":"object","properties":{"a":{"title":"A","type":"integer"},"b":{"title":"B","type":"integer"}},"required":["a","b"]}},{"name":"sub1","description":"","inputSchema":{"type":"object","properties":{"a":{"title":"A","type":"integer"},"b":{"title":"B","type":"integer"}},"required":["a","b"]}}]', 'sse', 'SSE', null, '1', null, '测试8-piz7Hngo-AIAE', null, '46ee3000571e4019a2d1039187aa0295', 'test_project_id', 'default', null,'workspace');