/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
 
INSERT
 INTO t_agent(agent_id, project_id, name, description, icon, instructions, trigger_list, prologue, suggest_queries, additional_questions_config, status, creator, creator_id, created_on, updated_on, published_on, deleted)
VALUES ('test_agent_id', 'test_project_id', '测试Agent', '测试Agent描述', 'data:image/png;base64,123', NULL, NULL, NULL, NULL, '{"enable":true,"rounds":3,"prompt":"1. 每次生成的问题不超过30个字。\n2. 生成问题的对话风格，与用户对话历史要一致，贴合用户对话场景。\n3. 不要生成相同或过于相似的问题。"}', 'published', 'test_creator', 'test_creator_id1', '2024-08-12 20:34:00', '2024-08-12 20:34:00', '2024-08-12 20:34:00', 0);