/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session.impl;

import com.openjiuwen.studio.agent.space.app.constant.LoginConstant;
import com.openjiuwen.studio.agent.space.app.service.session.LoginLogService;
import com.openjiuwen.studio.agent.space.app.service.session.util.SessionIdHasher;
import com.openjiuwen.studio.agent.space.app.util.DateUtils;
import com.openjiuwen.studio.agent.space.common.utils.SeqNoUtil;
import com.openjiuwen.studio.agent.space.dao.application.LoginLogMapper;
import com.openjiuwen.studio.agent.space.dao.entity.LoginLogEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoginLogServiceImpl implements LoginLogService {
    @Resource
    private LoginLogMapper loginLogMapper;

    @Resource
    private SessionIdHasher sessionIdHasher;

    @Override
    public void createLoginLog(LoginLogEntity loginLog) {
        String sessionIdHash = sessionIdHasher.hash(loginLog.getSessionId());
        loginLog.setSessionId(sessionIdHash);
        loginLog.setOperTime(DateUtils.getNowDateStr());
        loginLog.setId(SeqNoUtil.generateId(SeqNoUtil.LoginLog.LOGIN_LOG));
        loginLogMapper.insert(loginLog);
    }

    @Override
    public void logOutLoginLog(String sessionId, String logoutType, String content) {
        String sessionIdHash = sessionIdHasher.hash(sessionId);
        LoginLogEntity loginLogEntity = loginLogMapper.queryLogBySessionId(sessionIdHash, LoginConstant.LOGIN);
        LoginLogEntity logoutLogEntity = loginLogMapper.queryLogBySessionId(sessionIdHash, LoginConstant.LOGOUT);
        if (loginLogEntity != null && logoutLogEntity == null) {
            loginLogEntity.setId(SeqNoUtil.generateId(SeqNoUtil.LoginLog.LOGIN_LOG));
            loginLogEntity.setOperType(LoginConstant.LOGOUT);
            loginLogEntity.setOperTime(DateUtils.getNowDateStr());
            loginLogEntity.setLogoutType(logoutType);
            loginLogEntity.setSuccessFlag(Objects.isNull(content) ? 1 : 0);
            loginLogEntity.setContent(content);
            loginLogMapper.insert(loginLogEntity);
        }
    }

    @Override
    public void batchLogOutLoginLog(List<String> sessionIds, String logoutType) {
        sessionIds = sessionIds.stream().distinct().collect(Collectors.toList());
        List<LoginLogEntity> entities = loginLogMapper.queryLogBySessionIds(sessionIds, LoginConstant.LOGIN);
        if (!CollectionUtils.isEmpty(entities)) {
            List<LoginLogEntity> logoutEntities = loginLogMapper.queryLogBySessionIds(sessionIds, LoginConstant.LOGOUT);
            List<String> logoutSessionIds = CollectionUtils.isEmpty(logoutEntities)
                ? new ArrayList<>()
                : logoutEntities.stream().map(LoginLogEntity::getSessionId).toList();
            entities = entities.stream()
                .filter(item -> !logoutSessionIds.contains(item.getSessionId()))
                .collect(Collectors.toList());
            entities.forEach(item -> {
                item.setId(SeqNoUtil.generateId(SeqNoUtil.LoginLog.LOGIN_LOG));
                item.setOperType(LoginConstant.LOGOUT);
                item.setOperTime(DateUtils.getNowDateStr());
                item.setLogoutType(logoutType);
            });
            loginLogMapper.insertBatchSomeColumn(entities);
        }
    }
}
