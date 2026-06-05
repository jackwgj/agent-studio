/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.prompt.engineering.dto.FileInfoVo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PromptServiceTest {

    @InjectMocks
    private PromptService promptService;

    @Mock
    private PromptObsService promptObsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFileInfoVoByFileId() {
        String fildId = "";
        FileInfoVo vo = promptService.getFileInfoVoByFileId(fildId);
        assertNull(vo.getFileId());
        fildId = "file";
        FileInfoVo fileInfoVo = promptService.getFileInfoVoByFileId(fildId);
        assertNotNull(fileInfoVo);
    }
}
