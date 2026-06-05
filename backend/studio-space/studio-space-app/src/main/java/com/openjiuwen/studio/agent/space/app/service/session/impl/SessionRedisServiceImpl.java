/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session.impl;

import com.google.common.base.Preconditions;
import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;
import com.openjiuwen.studio.agent.space.app.service.session.SessionRedisService;
import com.openjiuwen.studio.agent.space.app.service.session.config.SessionProperties;
import com.openjiuwen.studio.agent.space.app.service.session.util.SessionIdHasher;
import com.openjiuwen.studio.agent.space.app.util.DateUtils;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.utils.JacksonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * session redis服务类
 */
@Slf4j
@Component
public class SessionRedisServiceImpl implements SessionRedisService {
    @Resource
    private RedisClient redisClient;

    @Resource
    private SessionIdHasher sessionIdHasher;

    @Resource
    private Ciphers ciphers;

    @Resource
    private SessionProperties sessionProperties;

    @Override
    public SessionInfoDetail querySession(String sessionId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(sessionId), "sessionId must be not empty");

        String sessionIdHash = sessionIdHasher.hash(sessionId);
        String redisKey = SESSION_PREFIX + sessionIdHash;
        SessionInfoDetail sessionInfo = null;
        String sessionInfoStr = redisClient.get(redisKey);
        if (StringUtils.isBlank(sessionInfoStr)) {
            sessionInfo = JacksonUtils.json2pojo(sessionInfoStr, SessionInfoDetail.class);
        }
        if (sessionInfo == null) {
            return null;
        }

        // 敏感信息解密
        sessionInfo.putSessionAttr(ContextConstants.AGENT_SPACE_IAM_TOKEN,
            ciphers.decrypt(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_IAM_TOKEN)));
        sessionInfo.setSessionId(sessionId);
        return sessionInfo;
    }

    @Override
    public void updateSession(SessionInfoDetail sessionInfo) {
        sessionInfo.setSessionId(sessionIdHasher.hash(sessionInfo.getSessionId()));

        String redisKey = SESSION_PREFIX + sessionInfo.getSessionId();
        sessionInfo.putSessionAttr(ContextConstants.AGENT_SPACE_IAM_TOKEN,
            ciphers.encrypt(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_IAM_TOKEN)));
        sessionInfo.setExpiredTime(DateUtils.asDate(LocalDateTime.now().plusMinutes(sessionProperties.getTimeOut())));
        sessionInfo.setMaxExpiredTime(
            DateUtils.asDate(LocalDateTime.now().plusMinutes(sessionProperties.getMaxTimeOut())));
        redisClient.set(redisKey, JacksonUtils.obj2json(sessionInfo),
            Duration.ofSeconds(sessionProperties.getRedisTimeOut() * 60));
    }
}