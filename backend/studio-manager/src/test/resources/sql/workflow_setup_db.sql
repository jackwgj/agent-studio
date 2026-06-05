/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, test_status)
VALUES ('test_code_interpreter_workflow_id', '代码执行', 'code_interpreter', '代码解释器工作流调用', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_code_interpreter_workflow_id.json', null, 1);

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_web_search_workflow_id', '网络搜索', 'web_search', '网络搜索工作流调用', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_web_search_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, last_version_id)
VALUES ('test_llm_workflow_id', '大厨师_', 'cooker', '大厨师就是大厨师_', 'data:image/png;base64,123', 'published', 1730792030,
        1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_llm_workflow_id.json', 'task', 'test_version_id2');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_llm_workflow_stream_id', '大厨师', 'cooker', '大厨师就是大厨师', 'data:image/png;base64,123', 'published',
        1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_llm_workflow_stream_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_llm_workflow_error_id', '大厨师', 'cooker', '大厨师就是大厨师', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_llm_workflow_error_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, last_version_id)
VALUES ('test_release_workflow', '大厨师', 'cooker', '大厨师就是大厨师', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_llm_workflow_error_id.json', 'task', '1738752428266');


INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1, dsl_path, ir_path)
VALUES ('test_release_id', '1738752428266', 'v20250205184705', '', 'test_release_workflow', 'workflow', 'normal', '1730792030', '1730792030', NULL, NULL, 'workflow/flow/test_llm_workflow_id/test_llm_workflow_id.json', 'workflow/ir/test_llm_workflow_id/test_llm_workflow_id.json');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type, last_version_id, ref_workflows)
VALUES ('test_parent_workflow', '大厨师', 'cooker', '大厨师就是大厨师', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_llm_workflow_error_id.json', 'task', '','|test_release_workflow|1738752428266|,');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_prologue_workflow_id', 'prologue测试', 'prologue', 'prologue测试', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_prologue_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_questions_workflow_id', 'questions测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_questions_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, dsl_path, ir_path, status, visibility, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, workflow_type, last_version_id, ref_workflows, test_status, icon_name)
VALUES ('test_workflow_scene_id', 'test0326', 'test0326', 'test0326', 'data:image/png;base64,123', 'workflow-ir/workflow/flow/test_workflow_scene_id.json', 'workflow-ir/workflow/ir/test_workflow_scene_id.json', 'published', 'global', 1742995277672, 1748412180770, 1748503257221, '华为官方', 'test_project_id', '官方预置', 'test_project_id', 'test_project_id', 'default', 'default', 1748412180770, 0, 'chat', '1748400865320', '', 0, NULL);

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_workflow_id', 'questions测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_questions_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, trigger_list, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_workflow_id10', '添加触发器测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published','[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_questions_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, trigger_list, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_workflow_id11', '删除触发器测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published','[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_questions_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow(id, name, code, description, avatar, status, trigger_list, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, workspace_id, deploy_wf_version, deleted, dsl_path, workflow_type)
VALUES ('test_workflow_id12', '编辑触发器测试', 'questions', 'questions测试', 'data:image/png;base64,123', 'published','[{"trigger_id":"test_trigger_id","name":"触发器1","type":"TIMER","cron":"0/10 * * * * ?","prompt":"打车"}]', 1730792030, 1730792030, 1730792030, 'jujiawei', 'test_user_id', 'jujiawei', 'test_user_id', 'test_project_id', 'test_domain_id', 'default', null, 0, 'workflow-ir/workflow/flow/test_questions_workflow_id.json', 'task');

INSERT
 INTO t_agent_workflow (id, name, code, description, avatar, icon_name, dsl_path, ir_path, status, trigger_list, visibility, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, deploy_wf_version, ref_workflows, last_version_id, deleted, test_status, customize_node, workflow_type, workspace_id)
VALUES ('test_workflow_id13', '验证1', 'yan1', '1', '', null, 'workflow/flow/83c226fa-aa02-49c9-b263-709118b54404/83c226fa-aa02-49c9-b263-709118b54404.json', 'workflow/ir/83c226fa-aa02-49c9-b263-709118b54404/83c226fa-aa02-49c9-b263-709118b54404.json', 'published', null, 'project', 1755843388310, 1756988130993, 1756988182125, '官方预置', '2', '官方预置', '2', 'test_project_id', '0', 1756988130993, null, '1756988182015', 0, 1, 1, 'chat', 'default');

INSERT
 INTO t_mapping (mapping_id, app_id, app_version, app_type, app_name, resource_id, resource_type, resource_name, resource_version, valid, resource_desc, created_on, updated_on)
VALUES ('test_workflow_mapping_id13', 'test_workflow_id13', null, 'workflow', 'yan1', 'preset_test0826', 'workflow', 'test0826', null, 0, null, '2025-08-29 11:45:49', '2025-08-30 14:36:54');

INSERT
 INTO t_structured_message (id, name, category, content, status, import_method, visibility,
                                  workspace_id, domain_id, user_id, project_id, created_on, creator_id,
                                  updated_on, updater_id)
VALUES ('123456789', 'test数据', '异常', '{\"additionalProp1\":{\"a\":1,\"b\":2}}', '0',
        'PAGE', 'workspace', 'default', '123456', '123456', '123456', '2025-09-25 19:22:19', '123456',
        '2025-09-25 19:22:19', '123456');

INSERT
 INTO t_agent_workflow (id, name, code, description, avatar, icon_name, dsl_path, ir_path, status, trigger_list, visibility, created_at, updated_at, published_at, created_by, creator_id, updated_by, updater_id, project_id, domain_id, deploy_wf_version, ref_workflows, last_version_id, deleted, test_status, customize_node, workflow_type, workspace_id)
VALUES ('test_workflow_id14', '验证1', 'yan1', '1', '', null, 'workflow/flow/83c226fa-aa02-49c9-b263-709118b54404/83c226fa-aa02-49c9-b263-709118b54404.json', 'workflow/ir/83c226fa-aa02-49c9-b263-709118b54404/83c226fa-aa02-49c9-b263-709118b54404.json', 'published', null, 'project', 1755843388310, 1756988130993, 1756988182125, '官方预置', '2', '官方预置', '2', 'test_project_id', '0', 1756988130993, null, '1756988182015', 0, 1, 1, 'chat', 'default_1');

