/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
INSERT INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on)
VALUES ('uuid1', 'test_version_id', 'test_version_name', 'note', 'test_agent_id', 'agent', 'normal', 'test_dsl_path', 'test_ir_path', 'user', 'user_id', '2024-08-13 17:30:28');

INSERT INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status, dsl_path, ir_path, creator, creator_id, released_on)
VALUES ('uuid2', 'test_version_id1', 'test_version_name1', 'note', 'test_agent_id', 'agent', 'normal', 'test_dsl_path', 'test_ir_path', 'user', 'user_id', '2024-08-13 17:30:28');


INSERT INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status,
                              dsl_path, ir_path, creator, creator_id, released_on)
VALUES ('uuid3', 'test_version_id2', 'test_version_name2', 'note', 'test_workflow_id', 'workflow', 'normal',
        'test_dsl_path', 'test_ir_path', 'user', 'user_id', '2024-08-13 17:30:28');

INSERT INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status,
                              dsl_path, ir_path, creator, creator_id, released_on)
VALUES ('uuid4', 'test_version_id3', 'test_version_name3', 'note', 'test_workflow_id', 'workflow', 'normal',
        'test_dsl_path', 'test_ir_path', 'user', 'user_id', '2024-08-13 17:30:28');

INSERT INTO t_release_version(id, version_id, version_name, version_note, app_id, app_type, status,
                              dsl_path, ir_path, creator, creator_id, released_on)
VALUES ('uuid5', 'test_version_id2', 'test_version_name2', 'note', 'test_llm_workflow_id', 'workflow', 'normal',
        'test_dsl_path', 'test_ir_path', 'user', 'test_user_id', '2024-08-13 17:30:28');