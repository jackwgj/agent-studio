/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT  INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at,
       published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_code_interpreter_workflow_id', '代码执行', 'code_interpreter', '代码解释器工作流调用','3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg',
       'published', 1730792030, 1730792030, 1730792030, 'jujiawei','test_user_id', 'jujiawei', 'test_user_id', 'test_project_id',
       'test_domain_id', 'default', null, 0,'workflow-ir/workflow/flow/test_code_interpreter_workflow_id.json', 'task');

INSERT  INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at,
       published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)

VALUES ('test_web_search_workflow_id', '网络搜索', 'web_search', '网络搜索工作流调用', '3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg',
       'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id',
       'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_web_search_workflow_id.json', 'task');

INSERT  INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at,
       published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)

VALUES ('test_complex_intent', '复杂意图', 'test_complex_intent', '复杂意图', '3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg',
       'published', 1730792030, 1730792030, 1730792030, '1', 'test_user_id', '1', 'test_user_id',
       'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_complex_intent.json', 'task');

INSERT  INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at,
       published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)

VALUES ('test_agent_workflow_id', 'Agent节点', 'test_agent_workflow_id', 'Agent节点', '3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg',
       'published', 1730792030, 1730792030, 1730792030, '1', 'test_user_id', '1', 'test_user_id',
       'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_agent_workflow_id.json', 'chat');


INSERT  INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1, dsl_path, ir_path)

VALUES ('test_code_interpreter_workflow_id', '1744720165224', 'v20250415202924', '', 'test_code_interpreter_workflow_id', 'workflow', 'normal', 'test_user_id', 'test_user_id', '2025-04-15 20:29:25', '', 'workflow-ir/workflow/flow/test_code_interpreter_workflow_id.json', 'workflow-ir/workflow/ir/test_code_interpreter_workflow_id.json');

INSERT INTO  t_model_service (ID, PROVIDER_ID, SERVICE_NAME, SERVICE_KEY, MODEL_NAME, MODEL_VERSION, MODEL_TYPE, MODEL_TAGS, MODEL_DESCRIPTION, MODEL_DEPLOY_TYPE, DOCUMENT_URL, MODEL_SIZE, CONTEXT_LENGTH, MODEL_PRIORITY, DOMAIN_ID, PROJECT_ID, WORKSPACE_ID, CREATED_BY_USER, LAST_UPDATED_BY_USER, CREATED_DATE, LAST_UPDATED_DATE, API_URL, IS_REASONING, IS_SUPPORT_FUNCTION, INTERFACE_PROTOCOL, IS_SUPPORT_STREAM, AUTH_METADATA_ID, PUBLISH_STATUS, IDENTITY_ID, SYSTEM_PROMPT, THROTTLING_POLICY) VALUES ('319f7fa7-da49-460e-9554-68fc329d410d', 'test_provider', 'agentBuilder4', 'publisher:agentBuilder4', 'agentBuilder4', 'agentBuilder4', 'LLM', '大语言模型,深度求索,中文,英文,对话问答,文案生成,代码生成,NL2SQL,任务规划,通用', '擅长通用对话任务', 'PLATFORM-INTEGRATION', 'resource_access/model_desc/deepseek/deepseek_model.md', 0, 0, 40, 'SYSTEM', 'test_project_id', 'default', 'SYSTEM', 'SYSTEM', 1753675200000, 1753675200000, 'https://127.0.0.1:8080/v1/model-router/chat/completions', 0, 1, 'STANDARD', 1, 'test_auth_meta_id', 'online', '64c8e9bb-7ded-4fc9-b63e-9ff3f0e7a588', NULL, -1);
