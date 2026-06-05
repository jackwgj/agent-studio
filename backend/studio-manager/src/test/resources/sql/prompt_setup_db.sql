/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */
INSERT INTO
t_pe_prompt(id, name, model, model_config, task_id, source, type, question, answer, variables, manual_score, creator, updater, created_on, updated_on)
VALUES ('test_prompt_id', 'test', 'agentBuilder', '{"n": null, "user": null, "token": null, "top_p": null, "endpoint": "https://xxx", "model_id": null, "max_tokens": 2048, "project_id": "test_project_id", "temperature": 0.1, "deployment_id": "xxx", "presence_penalty": 0.0}', 'test_prompt_task_id', 'CANDIDATE', 0, '我会提供一条评论信息给你，请判断这条评论属于“积极”、“中性”、“消极”三个标签中的哪个？从三个标签中选择一个，不要回答其他内容。 需要判断的评论为：“{{text}}”', '这条评论属于“积极”标签。因为使用了“我好喜欢这个样式呢！”这样的表达，显示出对某物的喜爱和正面的情感。', '[{\"key\": \"text\", \"name\": \"text\", \"value\": \"我好喜欢这个样式呢！\"}]', NULL, 'test', 'test', '2025-02-11 10:33:24', '2025-02-11 10:33:24');


