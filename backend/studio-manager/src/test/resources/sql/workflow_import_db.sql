/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT  INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at,
       published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_code_interpreter_workflow_id_100', '代码执行111111111 1111111111111111111111111111111111111111111111111', 'code_interpreter', '代码解释器工作流调用','3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg',
       'published', 1730792030, 1730792030, 1730792030, 'jujiawei','test_user_id', 'jujiawei', 'test_user_id', 'test_project_id',
       'test_domain_id', 0, null, 0,'workflow-ir/workflow/flow/test_code_interpreter_workflow_id.json', 'task');