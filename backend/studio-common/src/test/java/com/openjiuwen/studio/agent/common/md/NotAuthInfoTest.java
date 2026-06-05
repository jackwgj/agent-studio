/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.md;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import org.junit.jupiter.api.Test;

public class NotAuthInfoTest {
    @Test
    public void testType() {
        NotAuthInfo notAuthInfo = new NotAuthInfo();
        assertEquals("NO_AUTH", notAuthInfo.type(), "type() should return 'NO_AUTH'");
    }

    @Test
    public void testValidCheck() {
        NotAuthInfo notAuthInfo = new NotAuthInfo();
        // validCheck() should not throw any exceptions
        notAuthInfo.validCheck();
    }

    @Test
    public void testConvertToEncryptStr() {
        NotAuthInfo notAuthInfo = new NotAuthInfo();
        assertEquals("{}", notAuthInfo.convertToEncryptStr(),
            "convertToEncryptStr() should return '{}'");
    }

    @Test
    public void testAuthInfoTemplate() {
        NotAuthInfo notAuthInfo = new NotAuthInfo();
        assertEquals("{}", notAuthInfo.authInfoTemplate(),
            "authInfoTemplate() should return '{}'");
    }

    @Test
    public void testConvertToMaskStr() {
        NotAuthInfo notAuthInfo = new NotAuthInfo();
        assertEquals("{}", notAuthInfo.convertToMaskStr(true),
            "convertToMaskStr(true) should return '{}'");
        assertEquals("{}", notAuthInfo.convertToMaskStr(false),
            "convertToMaskStr(false) should return '{}'");
    }
}
