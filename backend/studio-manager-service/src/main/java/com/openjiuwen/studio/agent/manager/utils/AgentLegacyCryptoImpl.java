/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.common.service.service.LegacyCryptoHandler;

import org.springframework.stereotype.Component;

// 这里既能看到 MgSccCryptoUtils (同模块)，又能看到 LegacyCryptoHandler (依赖了common)
@Component
public class AgentLegacyCryptoImpl implements LegacyCryptoHandler {

    @Override
    public String encrypt(String text) {
        // 这一行就是连接点！
        return CryptoUtils.encrypt(text);
    }

    @Override
    public String decrypt(String text) {
        return CryptoUtils.decrypt(text);
    }
}
