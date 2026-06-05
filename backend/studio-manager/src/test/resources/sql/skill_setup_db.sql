/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
INSERT INTO `t_skill` (`skill_id`, `domain_id`, `name`, `icon`, `status`, `source`, `description`, `creator_id`,
                       `creator_name`, `created_at`, `updated_at`, `latest_version`, `used_version`)
VALUES ('test_skill_id', '0c72472bfe4647168124456891091495', 'test_agent_skill',
        'bdf8e247-9ff8-4b1f-9148-040b7c288894.png', 'deploy', 'custom', '123123', '6f80301edacc43728bf47b6a6484d512',
        'test', '1774530861889', '1774530861889', '111',
        'test_used_version');

INSERT INTO `t_skill_version` (`id`, `skill_id`, `version_name`, `used`, `description`, `obs_path`,
                               `creator_id`, `creator_name`, `created_at`, `name`)
VALUES ('test_used_version', 'test_skill_id', '3902sdc', 1,
        '会议录音转写文本分析与纪要生成。自动提取核心议题、决议及待办事项，过滤口语噪音并生成结构化文档。用于企业会议记录整理与任务跟踪。',
        'test', '6f80301edacc43728bf47b6a6484d512', 'test', '1774530861889',
        '会议纪要生成专家-1');

