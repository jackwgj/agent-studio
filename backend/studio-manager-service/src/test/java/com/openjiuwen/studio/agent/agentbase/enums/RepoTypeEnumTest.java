/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class RepoTypeEnumTest {
    private RepoTypeEnum repoTypeEnum = RepoTypeEnum.SHARE;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(repoTypeEnum, "value", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_fromValue_should_return_not_null() {
        RepoTypeEnum result = RepoTypeEnum.fromValue("not_empty");
        assertEquals(RepoTypeEnum.SHARE, result);
    }

    @Test
    void test_getValue_should_equal_result() {
        String result = repoTypeEnum.getValue();
        assertEquals("not_empty", result);
    }
}