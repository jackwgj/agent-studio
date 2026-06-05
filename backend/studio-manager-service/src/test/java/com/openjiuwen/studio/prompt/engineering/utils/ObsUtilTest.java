/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

import com.obs.services.ObsClient;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * 功能描述
 *
 */
class ObsUtilTest {
    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsClient obsClient;

    @InjectMocks
    private ObsUtil obsUtil;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(obsUtil, "obsClient", obsClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_checkZipBomb_IoException() {
        assertThrows(AgentStudioException.class, () -> {
            MockedConstruction<ZipInputStream> mocked = Mockito.mockConstruction(ZipInputStream.class, (mock, context) -> {
                doThrow(IOException.class).when(mock).getNextEntry();
            });
            try {
                obsUtil.checkZipBomb(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                });
            } finally {
                mocked.close();
            }
        });
    }
}
