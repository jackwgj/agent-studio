/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.plugin;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;
import com.openjiuwen.studio.agent.manager.service.plugin.PluginService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * 专门针对 PluginService 中 private String maskKey(String key) 方法的单元测试
 * 目标：达到 100% 的行覆盖率和分支覆盖率
 */
@ExtendWith(MockitoExtension.class)
public class PluginServiceMaskKeyTest {

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @InjectMocks
    private PluginService pluginService; // 如果你的类名叫 PluginBaseImpl，请在这里修改

    @Test
    @DisplayName("分支 1：入参 key 为空字符串，直接返回脱敏占位符")
    void testMaskKey_BlankKey() {
        String result1 = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", (String) null);
        String result2 = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", "   ");

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result1);
        assertEquals(CommonConstant.ANONYMIZED_TEXT, result2);
    }

    @Test
    @DisplayName("分支 2：解密时抛出 AgentStudioException (如跨租户隔离)，捕获并返回占位符")
    void testMaskKey_ThrowsAgentStudioException() {
        String cipherKey = "E1-{uuid}-test";
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new AgentStudioException(StudioError.KMS_CRYPTO_ERROR));

        String result = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", cipherKey);

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("分支 3：解密时抛出通用 Exception，捕获并返回占位符")
    void testMaskKey_ThrowsGeneralException() {
        String cipherKey = "E1-{uuid}-test";
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new RuntimeException("Unexpected KMS Error"));

        String result = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", cipherKey);

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("分支 4：解密成功，但明文为空，返回脱敏占位符")
    void testMaskKey_DecryptedKeyIsBlank() {
        String cipherKey = "cipher_key";
        when(encryptionAdapter.decrypt(cipherKey)).thenReturn(""); // 模拟解密出空字符串

        String result = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", cipherKey);

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("分支 5：解密成功，且明文长度刚好为 1，首字母+占位符")
    void testMaskKey_DecryptedKeyLengthIsOne() {
        String cipherKey = "cipher_key";
        String decryptedKey = "A"; // 长度为 1
        when(encryptionAdapter.decrypt(cipherKey)).thenReturn(decryptedKey);

        String result = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", cipherKey);

        // 预期：首字母 'A' 拼接 ANONYMIZED_TEXT 去掉第一个字符后的部分
        String expected = "A" + CommonConstant.ANONYMIZED_TEXT.substring(1);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("分支 6：解密成功，且明文长度大于 1，首字母+占位符+尾字母")
    void testMaskKey_DecryptedKeyLengthGreaterThanOne() {
        String cipherKey = "cipher_key";
        String decryptedKey = "HelloWorld";
        when(encryptionAdapter.decrypt(cipherKey)).thenReturn(decryptedKey);

        String result = ReflectionTestUtils.invokeMethod(pluginService, "maskKey", cipherKey);

        String expected = "H" + CommonConstant.ANONYMIZED_TEXT.substring(1,
                CommonConstant.ANONYMIZED_TEXT.length() - 1) + "d";
        assertEquals(expected, result);
    }
}
