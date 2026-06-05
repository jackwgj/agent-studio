/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mock;

import org.springframework.stereotype.Service;

/**
 * 启动mocker server
 *
 */
@Service
public class MockerStarter {
    static {
        MockServer.init();
    }
}
