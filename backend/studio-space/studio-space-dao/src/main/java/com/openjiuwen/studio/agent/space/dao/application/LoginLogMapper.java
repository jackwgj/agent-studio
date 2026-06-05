/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.application;

import com.openjiuwen.studio.agent.space.dao.entity.LoginLogEntity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {

    int insertBatchSomeColumn(List<LoginLogEntity> loginLogEntities);

    default LoginLogEntity queryLogBySessionId(String sessionId, String operType){
        return selectOne(new LambdaQueryWrapper<LoginLogEntity>().eq(LoginLogEntity::getSessionId, sessionId).eq(LoginLogEntity::getOperType, operType));
    }

    default List<LoginLogEntity> queryLogBySessionIds(List<String> sessionIds, String operType){
        return selectList(new LambdaQueryWrapper<LoginLogEntity>().in(LoginLogEntity::getSessionId, sessionIds).eq(LoginLogEntity::getOperType, operType));
    }
}
