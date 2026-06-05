/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, last_version_id, ref_workflows)
VALUES ('start_wf', 'start_wf名字', 'start_wfCode', 'start_wf描述', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 0, null, 0, 'workflow/flow/start_wf/start_wf.json', 'task', '','|test_release_workflow|1738752428266|,');
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, extend1)
VALUES ('start_wf_version', '1744203795942', 'v20250409210314', '', 'start_wf', 'workflow', 'normal', 'workflow/flow/start_wf/start_wf_1744203795942.json', 'workflow/ir/b967e09d-d76b-4495-aa11-fdedf93531a4/b967e09d-d76b-4495-aa11-fdedf93531a4_1744203795942.json', 'test_user_id', 'test_user_id', '2025-04-17 21:43:14', '');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('end_wf', 'end_wf名字', 'end_wfCode', 'end_wf描述', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 0, null, 0, 'workflow/flow/end_wf.json', 'task');
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, extend1)
VALUES ('end_wf_version', '1744203795943', 'v20250409210315', '', 'end_wf', 'workflow', 'normal', 'workflow/flow/end_wf/end_wf_1744203795943.json', 'workflow/ir/end_wf/end_wf_1744203795943.json', 'test_user_id', 'test_user_id', '2025-04-17 21:43:14', '');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('workflow1_wf', 'workflow1_wf名字', 'workflow1_wfCode', 'workflow1_wf描述', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 0, null, 0, 'workflow/flow/workflow1_wf.json', 'task');
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, extend1)
VALUES ('workflow1_wf_version', '1744203795944', 'v20250409210315', '', 'workflow1_wf', 'workflow', 'normal', 'workflow/flow/workflow1_wf/workflow1_wf_1744203795944.json', 'workflow/ir/workflow1_wf/workflow1_wf_1744203795944.json', 'test_user_id', 'test_user_id', '2025-04-17 21:43:14', '');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('intentionWorkflow_wf', 'intentionWorkflow_wf名字', 'intentionWorkflow_wfCode', 'intentionWorkflow_wf描述', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 0, null, 0, 'workflow/flow/intentionWorkflow_wf.json', 'task');
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, extend1)
VALUES ('intentionWorkflow_wf_version', '1744203795945', 'v20250409210315', '', 'intentionWorkflow_wf', 'workflow', 'normal', 'workflow/flow/intentionWorkflow_wf/intentionWorkflow_wf_1744203795945.json', 'workflow/ir/workflow1_wf/workflow1_wf_1744203795945.json', 'test_user_id', 'test_user_id', '2025-04-17 21:43:14', '');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('workflow_default_wf', 'workflow_default_wf名字', 'workflow_default_wfCode', 'workflow_default_wf描述', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 0, null, 0, 'workflow/flow/workflow_default_wf.json', 'task');
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, extend1)
VALUES ('workflow_default_wf_version', '1744203795946', 'v20250409210316', '', 'workflow_default_wf', 'workflow', 'normal', 'workflow/flow/workflow_default_wf/workflow_default_wf_1744203795946.json', 'workflow/ir/workflow_default_wf/workflow_default_wf_1744203795946.json', 'test_user_id', 'test_user_id', '2025-04-17 21:43:14', '');

