/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestParamConstantsTest {

    @Test
    void testRepoId() {
        assertEquals("repo_id", RequestParamConstants.REPO_ID);
    }

    @Test
    void testName() {
        assertEquals("name", RequestParamConstants.NAME);
    }

    @Test
    void testPage() {
        assertEquals("page_num", RequestParamConstants.PAGE);
    }

    @Test
    void testPageSize() {
        assertEquals("page_size", RequestParamConstants.PAGE_SIZE);
    }

    @Test
    void testDocTypeKwdImage() {
        assertEquals("image", RequestParamConstants.DOC_TYPE_KWD_IMAGE);
    }
}
