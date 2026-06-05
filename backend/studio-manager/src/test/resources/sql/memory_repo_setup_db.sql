/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */


INSERT INTO `t_memory_repo` (`id`, `name`, `description`, `icon`, `time_span`, `conversation_round`, `long_term_memory_strategies`, `project_id`, `workspace_id`, `domain_id`, `created_user_id`, `created_user_name`, `last_update_user_id`, `last_update_user_name`, `create_time`, `update_time`) VALUES ('test_memory_repo_id', '智能记忆库', '用于存储项目关键信息的智能记忆库', NULL, NULL, NULL, '[{\"type\":\"semantic_memory\",\"prompt\":\"用户对话中涉及的与时间无明确关系的事实性内容或概念。包括但不限于：解释、描述、关系、概念理解、背景信息等等\"},{\"type\":\"user_profile\",\"prompt\":\"用户本人的肯定或否定表述。包括但不限于：个人偏好（在食物、产品、活动等类别的喜恶偏好）、个人信息（姓名、关系、工作职业等信息）、个人计划（未来事件、目标等任何用户分享的计划）、其他信息（其他任何对未来对话有益的重要用户本人个人事实）\"}]', 'b691749589164e26a276ad287100cc79', 'default', '0c72472bfe4647168124456891091495', '6f80301edacc43728bf47b6a6484d512', 'agent_builder', '6f80301edacc43728bf47b6a6484d512', 'agent_builder', '2026-03-24 15:22:32', '2026-03-24 15:22:32');
