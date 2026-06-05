/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;

import lombok.Data;

import java.util.List;

/**
 * IR中的凭证
 *
 */
@Data
public class Credential {
    /**
     * 资源名称
     */
    private String name;

    private AuthInfo.ScopeEnum scope;

    private AuthInfo.DomainEnum domain;

    private List<AuthKey> authKeys;

    @Data
    public static class AuthKey {
        private String authName;

        private String authValue;
    }

}
