/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 记忆库数据访问层
 */
@Mapper
public interface MemoryRepoMapper {

    /**
     * 插入记忆库记录
     */
    int insert(MemoryRepoEntity memoryRepo);

    /**
     * 根据ID删除记忆库
     */
    int deleteById(@Param("id") String id);

    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 更新记忆库信息
     */
    int updateById(MemoryRepoEntity memoryRepo);

    /**
     * 根据ID查询记忆库
     */
    MemoryRepoEntity selectById(@Param("id") String id);

    /**
     * 根据ID和空间ID查询记忆库
     */
    MemoryRepoEntity selectByIdAndWorkspaceId(@Param("id") String id, @Param("workspaceId") String workspaceId);

    /**
     * 条件查询记忆库列表
     */
    List<MemoryRepoEntity> selectByCondition(@Param("projectId") String projectId,
                                             @Param("workspaceId") String workspaceId,
                                             @Param("domainId") String domainId,
                                             @Param("ids") List<String> ids,
                                             @Param("name") String name);
}
