/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.entity.SkillEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SIESkillMapper {
    /**
     * 插入 Skill 基本信息
     * @return 新建的版本 id
     */
    int insert(SkillEntity skill);

    /**
     * 更新 Skill 基本信息
     * @return 更新的行数
     */
    int update(@Param("skillId") String skillId, @Param("skill") SkillEntity skill);

    /**
     * 删除 Skill 基本信息
     * @return 删除的行数
     */
    int delete(String skillId);

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

    /**
     * 根据 skillIds 批量查询 Skills
     */
    List<SkillDetails> searchBySkillIds(@Param("skillIds") List<String> skillIds);

    /**
     * 根据 Skill Id 和 domain Id 查询
     */
    SkillDetails searchBySkillIdAndDomainId(@Param("skillId") String skillId, @Param("domainId") String domainId);
}
