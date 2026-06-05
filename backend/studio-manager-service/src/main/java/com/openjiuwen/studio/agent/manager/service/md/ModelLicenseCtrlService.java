/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelLicenseCtrlService {

    /**
     * 是否可以接入模型
     */
    public void canAccessIntegrationModel() {
        // 待扩展
    }

    public boolean canUseFreeModel() {
        // 待扩展
        return false;
    }
}
