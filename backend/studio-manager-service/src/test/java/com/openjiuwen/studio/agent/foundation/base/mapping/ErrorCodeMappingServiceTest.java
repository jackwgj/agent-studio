/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ErrorCodeMappingServiceTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.foundation.base.mapping.ErrorCodeMappingService errorCodeMappingService;

    @BeforeEach
    void setUp() {
        errorCodeMappingService.loadMappingConfig();
    }

    @Test
    void test_getAgentBaseErrorCode() {
        errorCodeMappingService.getAgentBaseErrorCode("KOS.00010001");
    }
}
