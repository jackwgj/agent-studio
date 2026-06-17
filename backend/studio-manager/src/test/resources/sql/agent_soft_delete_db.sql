-- Agent 软删除测试数据

-- 工作空间表数据
INSERT INTO t_workspace (id, project_id, domain_id, name, tenant_id, created_on, updated_on)
VALUES ('default', 'test_project_id', 'test_domain_id', 'default', 'test_domain_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Agent 表数据（包含所有 NOT NULL DEFAULT 列的显式值）
INSERT INTO t_agent (agent_id, project_id, name, status, creator, creator_id, created_on, updated_on, deleted, workspace_id, domain_id, workflow_switch_enabled, scheduling_mode, is_share)
VALUES ('test_agent_for_delete', 'test_project_id', 'Agent For Delete', 'DRAFT', 'test_creator', 'test_creator_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'default', 'test_domain_id', 0, 'ReAct', 0);

INSERT INTO t_agent (agent_id, project_id, name, status, creator, creator_id, created_on, updated_on, deleted, workspace_id, domain_id, workflow_switch_enabled, scheduling_mode, is_share)
VALUES ('test_agent_with_releases', 'test_project_id', 'Agent With Releases', 'DRAFT', 'test_creator', 'test_creator_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'default', 'test_domain_id', 0, 'ReAct', 0);

INSERT INTO t_agent (agent_id, project_id, name, status, creator, creator_id, created_on, updated_on, deleted, workspace_id, domain_id, workflow_switch_enabled, scheduling_mode, is_share)
VALUES ('test_agent_history', 'test_project_id', 'Agent History', 'DRAFT', 'test_creator', 'test_creator_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'default', 'test_domain_id', 0, 'ReAct', 0);

-- 发布版本表数据
INSERT INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on, deleted)
VALUES ('version_1', 'version_id_1', 'v1.0', 'Test version', 'test_agent_with_releases', 'agent', 'normal', '/path/to/dsl', '/path/to/ir', 'test_creator', 'test_creator_id', CURRENT_TIMESTAMP, 0);