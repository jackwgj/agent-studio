/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.SkillVersionEntity;

import java.util.List;

public interface SkillVersionMapper {

    /**
     * 插入 Skill 版本信息
     * @return 新建的版本 id
     */
    int insert(SkillVersionEntity skillVersion);

    /**
     * 更新 Skill 某个版本的信息
     * @return 更新的行数s
     */
    int update(SkillVersionEntity skillVersion);

    /**
     * 删除 Skill 的某个版本
     * @return 删除的行数
     */
    int delete(String versionId);

    /**
     * 删除某个 Skill 的所有版本
     */
    void deleteBySkillId(String skillId);

    /**
     * 通过 Skill Id 获取它的所有版本信息
     */
    List<SkillVersionEntity> searchBySkillId(String skillId, int offset, int limit);

    /**
     * 通过 Version Id 获取版本信息
     */
    SkillVersionEntity searchByVersionId(String versionId);

    /**
     * 通过 Skill Id 和 Version 获取版本信息
     */
    SkillVersionEntity searchBySkillIdAndVersion(String skillId, String version);

    /**
     * 获取 Skill Id 对应的版本总数
     */
    int countVersionBySkillId(String skillId);
}
