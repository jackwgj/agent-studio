/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection;

import lombok.Data;

import java.util.Objects;

@Data
public class LsBasicAuth {

    String lakeSearchAuthorization;

    public Boolean isValid() {
        Objects.requireNonNull(lakeSearchAuthorization, "lakeSearchAuthorization should not be null");
        return true;
    }
}
