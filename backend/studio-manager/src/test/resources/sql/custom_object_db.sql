-- Custom Object 测试数据

-- 工作空间表数据
INSERT INTO t_workspace (id, project_id, domain_id, name, tenant_id, created_on, updated_on)
VALUES ('default', 'test_project_id', 'test_domain_id', 'default', 'test_domain_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);