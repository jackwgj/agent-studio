/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.model.session;

import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import lombok.Data;

import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * session信息
 */
@Data
public class SessionInfoDetail {
    /**
     * token
     */
    private String sessionId;

    /**
     * 用户Id
     */
    private String userId;

    private Date createTime;

    private Date updateTime;

    private Date expiredTime;

    private Date maxExpiredTime;

    private String ticket;

    private String csrfToken;

    private String domainId;

    private Map<String, String> sessionAttrs = new HashMap<>();

    public String getSessionAttr(String key) {
        if (CollectionUtils.isEmpty(sessionAttrs) || StringUtil.isEmptyString(key)) {
            return null;
        }
        return sessionAttrs.get(key);
    }

    public void putSessionAttr(String key, String value) {
        if (CollectionUtils.isEmpty(sessionAttrs) || StringUtil.isEmptyString(key)) {
            return;
        }
        sessionAttrs.put(key, value);
    }
}
