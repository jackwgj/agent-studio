/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApiGroupsDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName : ApicApisMapper
 * @Description :
 * @Date : Created in 2024/4/8 19:10
 * @Version : V1.0
 */
@Mapper
public interface ApigApiGroupsMapper {

    ApigApiGroupsDto selectById(@Param("id") String id);

    ApigApiGroupsDto selectByName(@Param("name") String name);

    void insert(@Param("apigApiGroupsDto") ApigApiGroupsDto apigApiGroupsDto);

    void deleteById(@Param("id") String id);

    void deleteByName(@Param("name") String name);

    Integer getNullDeptCodeNum();

    int updateNullDeptCodeByUserId(@Param("deptCode") String deptCode, @Param("userId") String userId);
}
