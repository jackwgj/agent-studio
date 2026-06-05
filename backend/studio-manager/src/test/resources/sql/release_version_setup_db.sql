/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1, dsl_path)
VALUES ('uuid', 'test_version_id', 'v1', 'test_version_note', 'test_code_interpreter_workflow_id', 'workflow', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','', 'workflow-ir/workflow/flow/test_code_interpreter_workflow_id_test_version_id.json');

INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1)
VALUES ('uuid1', '1740991104999', 'v1', 'test_version_note', 'test_llm_workflow_id', 'workflow', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','|test_web_search_workflow_id|1740991104999|');

INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1)
VALUES ('uuid2', '1740991104999', 'v1', 'test_version_note', 'test_web_search_workflow_id', 'workflow', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','|test_llm_workflow_stream_id|1740991104999|');

INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1)
VALUES ('uuid3', '1740991104999', 'v1', 'test_version_note', 'test_llm_workflow_stream_id', 'workflow', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','|test_web_search_workflow_id|1740991104999|');

INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1, dsl_path)
VALUES ('uuid4', '1748400865320', 'v1', 'test_version_note', 'test_workflow_scene_id', 'workflow', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','|test_web_search_workflow_id|1740991104999|', 'workflow-ir/workflow/flow/test_workflow_scene_id.json');

INSERT
 INTO t_release_version (id, version_id, version_name, version_note, app_id, app_type, status, creator, creator_id, released_on, extend1, dsl_path)
VALUES ('uuid5', '1748400865320', 'v1', 'test_version_note', 'reserve_meeting_room_9', 'tool', 'normal', 'test_creator', 'test_user_id', '2024-08-12 20:34:00','|test_web_search_workflow_id|1740991104999|', 'tool/dsl/reserve_meeting_room_9.json');