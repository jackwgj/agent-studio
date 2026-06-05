/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApisDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName : ApicApisMapper
 * @Description :
 * @Date : Created in 2024/4/8 19:10
 * @Version : V1.0
 */
@Mapper
public interface ApigApisMapper {
    ApigApisDto selectById(@Param("id") String id);

    ApigApisDto selectByName(@Param("name") String name);

    List<ApigApisDto> selectByTenantId(@Param("tenant_id") String tenantId);

    void insert(@Param("apigApisDto") ApigApisDto apigApisDto);

    void update(@Param("apigApisDto") ApigApisDto apigApisDto);

    void hardDeleteById(@Param("id") String id);

    void deleteById(@Param("apigApisDto") ApigApisDto apigApisDto);

    void deleteByName(@Param("apigApisDto") ApigApisDto apigApisDto);

    void updateThrottlingPolicy(@Param("apigApisDto") ApigApisDto apigApisDto);

    List<ApigApisDto> selectDeleted();

    Integer getNullDeptCodeNum();

    int updateNullDeptCodeByUserId(@Param("deptCode") String deptCode, @Param("userId") String userId);
}
