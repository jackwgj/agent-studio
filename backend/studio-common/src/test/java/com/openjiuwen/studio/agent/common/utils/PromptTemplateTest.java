/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openjiuwen.studio.agent.common.utils.CommonUtils;
import com.openjiuwen.studio.agent.common.utils.KV;
import com.openjiuwen.studio.agent.common.utils.PromptTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class PromptTemplateTest {
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testPrompt() {
        String prompt = CommonUtils.getResourceContent("test_prompt.pt");
        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        promptTemplate.validateTemplate();

        assertNotNull(promptTemplate.format(KV.of("a", "b")));
        assertNotNull(promptTemplate.format(List.of(KV.of("a", "b"))));
    }

}
