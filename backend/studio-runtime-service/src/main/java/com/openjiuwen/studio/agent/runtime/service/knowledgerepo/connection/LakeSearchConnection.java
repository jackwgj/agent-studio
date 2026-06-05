/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection;


import com.openjiuwen.studio.agent.common.dto.knowledge.KerberosAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.enums.RagAuthMode;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
public class LakeSearchConnection extends KnowledgeSourceConnection {

    private String authMode;

    private boolean ocrEnable = false;

    private KerberosAuth kerberosAuth;

    private LsBasicAuth lsBasicAuth;

    public boolean isValid() {
        Objects.requireNonNull(authMode, "authMode should not be null");
        Objects.requireNonNull(getEndpoint(), "endpoint should not be null");
        RagAuthMode ragAuthMode = RagAuthMode.valueOf(authMode.toUpperCase(Locale.ENGLISH));
        switch (ragAuthMode) {
            case NONE -> {
                return true;
            }
            case BASIC -> {
                return lsBasicAuth.isValid();
            }
            default -> throw new AgentStudioException(StudioError.LAKE_SEARCH_AUTH_INVALID);
        }
    }
}
