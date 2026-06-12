/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.EnvironmentManagerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 工具Mapper
 *
 */
@Mapper
public interface EnvironmentManagerMapper {


    List<EnvironmentManagerEntity> selectAll(@Param("projectId") String projectId, @Param("pageOffset") int pageOffset, @Param("pageSize") int pageSize);

    List<EnvironmentManagerEntity> selectAllByStatus(@Param("status")String status, @Param("projectId") String projectId, @Param("pageOffset") int pageOffset, @Param("pageSize") int pageSize);

    List<EnvironmentManagerEntity> findByProjectId(@Param("projectId") String projectId);

    List<String> findAllWithGroupByProjectId();

    List<EnvironmentManagerEntity> findByProjectIdAndIsDefaultTrue(@Param("projectId") String projectId);

    List<EnvironmentManagerEntity> findByProjectIdAndIsDefaultFalseAndStatus(@Param("projectId") String projectId, @Param("status") String status);

    int countByProjectId(@Param("projectId") String projectId);

    List<EnvironmentManagerEntity> findByNameAndProjectId(@Param("name") String name, @Param("projectId") String projectId);

    EnvironmentManagerEntity findByIdAndProjectId(@Param("id") String id, @Param("projectId") String projectId);

    EnvironmentManagerEntity findById(@Param("id") String id);

    int batchUpdateIsDefaultByIds(@Param("ids") List<String> ids,@Param("isDefault") boolean isDefault, @Param("updaterId") String updaterId);

    int updateIsDefaultById(@Param("id") String id,@Param("isDefault") boolean isDefault, @Param("updaterId") String updaterId);

    int updateStatusById(@Param("id") String id,@Param("status") String status, @Param("updaterId") String updaterId);

    int updateStatusAndIsDefaultById(@Param("id") String id,@Param("status") String status, @Param("updaterId") String updaterId, @Param("isDefault") boolean isDefault);

    int insert(EnvironmentManagerEntity entity);

    int updateById(EnvironmentManagerEntity entity);

    int deleteById(@Param("id") String id);

    // 按条件分页查询（支持名称模糊匹配）
    List<EnvironmentManagerEntity> selectByConditionWithPage(@Param("name") String name, @Param("projectId") String projectId, @Param("offset") int offset, @Param("limit") int limit);

    // 按状态分页查询
    List<EnvironmentManagerEntity> selectByStatusConditionWithPage(@Param("status") String status, @Param("projectId") String projectId, @Param("offset") int offset, @Param("limit") int limit);

    // 计数方法（用于分页）
    int countByCondition(@Param("name") String name, @Param("projectId") String projectId);
    int countByStatus(@Param("status") String status, @Param("projectId") String projectId);
}
