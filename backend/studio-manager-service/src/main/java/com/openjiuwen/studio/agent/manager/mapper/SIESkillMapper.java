/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.entity.SkillEntity;

import java.util.List;

public interface SIESkillMapper {
        /**
     * 模糊搜索
     */
    List<SkillDetails> search(SkillEntity skill, int offset, int limit, String priorityStatus, String tagId, Integer publishedAsset);

    /**
     * 获取符合要求的 Skill 的数量
     */
    int countSelectedSkills(SkillEntity skill);
    /**
     * 查询 Skill 详情
     */
    SkillDetails searchBySkillId(String skillId);
}
