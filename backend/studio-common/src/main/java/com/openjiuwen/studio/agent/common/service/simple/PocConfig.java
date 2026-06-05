/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.service.simple;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class PocConfig {
    @Value("${poc.user.default-user-id:testUser}")
    private String defaultUserId;

    @Value("${poc.user.default-project-id:0}")
    private String defaultProject;

    @Value("${poc.user.default-domain-id:0}")
    private String defaultDomain;
}
