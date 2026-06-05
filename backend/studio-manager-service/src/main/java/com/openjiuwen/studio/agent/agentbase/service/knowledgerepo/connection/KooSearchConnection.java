/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection;

import com.openjiuwen.studio.agent.common.dto.knowledge.KooSearchAuthMode;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeSourceConnection;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
public class KooSearchConnection extends KnowledgeSourceConnection {

    private String authMode;

    private String applicationId;

    private String userName;

    private String appCode;

    // 加密后的值
    private String userPassword;

    private String domainName;

    private String projectName;

    private String projectId;

    private boolean ocrEnable = false;

    public boolean isValid() {
        Objects.requireNonNull(super.getEndpoint(), "endpoint should not be null");
        Objects.requireNonNull(this.applicationId, "applicationId should not be null");
        Objects.requireNonNull(this.projectId, "domainName should not be null");
        KooSearchAuthMode kooSearchAuthMode = KooSearchAuthMode.valueOf(authMode.toUpperCase(Locale.ENGLISH));
        switch (kooSearchAuthMode) {
            case TOKEN -> {
                Objects.requireNonNull(this.userName, "userName should not be null");
                Objects.requireNonNull(this.userPassword, "userPassword should not be null");
                Objects.requireNonNull(this.domainName, "domainName should not be null");
                Objects.requireNonNull(this.projectName, "domainName should not be null");
                return true;
            }
            case APP_CODE -> {
                Objects.requireNonNull(this.appCode, "appCode should not be null");
                return true;
            }
        }
        throw new AgentBaseException(ErrorCode.KOO_SEARCH_AUTH_MODE_ERROR);
    }
}
