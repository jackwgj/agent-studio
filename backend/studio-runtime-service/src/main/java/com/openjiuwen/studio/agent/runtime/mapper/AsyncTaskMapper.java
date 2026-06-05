/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.openjiuwen.studio.agent.runtime.entity.TaskEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 任务数据库mapper
 *
 */
@Mapper
public interface AsyncTaskMapper {
    /**
     * 插入单条数据
     *
     * @param entity 数据
     * @return affect row
     */
    int createEntity(@Param("entity") TaskEntity entity);

    /**
     * 搜索数据
     *
     * @param entity entity
     * @param sort 排序字段
     * @param order 排序方式
     * @return entity
     */
    List<TaskEntity> getTaskEntity(@Param("entity") TaskEntity entity, @Param("sort") String sort,
        @Param("order") String order);

    /**
     * 按主键更新
     *
     * @param entity entity
     * @return entity
     */
    int updateByPrimaryKey(@Param("entity") TaskEntity entity);

    /**
     * 获取初始化状态的工作流
     *
     * @return entity
     */
    List<TaskEntity> getInitWorkflowTaskEntity(@Param("limit") int limit);

    /**
     * 获取超期的工作流
     *
     * @return entity
     */
    List<TaskEntity> getExpiredTaskEntity(@Param("expireTime") Date expireTime);

    /**
     * 获取初始化状态的工作流
     */
    int deleteByIds(@Param("ids") List<String> ids);
}
