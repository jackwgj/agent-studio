/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, test_status, last_version_id)
VALUES ('custom_workflow', 'custom_workflow', 'custom_workflow', 'desc', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_op_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_code_interpreter_workflow_id.json', null, 1, 'version_id');