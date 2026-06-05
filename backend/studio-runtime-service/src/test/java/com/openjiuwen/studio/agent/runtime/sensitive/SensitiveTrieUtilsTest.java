/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;

import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.service.security.SecurityService;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 功能描述
 *
 */
public class SensitiveTrieUtilsTest {
    private SensitiveTrieUtils sensitiveTrieUtils;

    private SecurityService mockSecurityService;

    @BeforeEach
    public void setUp() throws Exception {
        try (MockedStatic<SpringBeanUtils> mockedStaticSpringBeanUtils =
                     mockStatic(SpringBeanUtils.class, RETURNS_DEEP_STUBS)) {
            // Mock 敏感词树缓存
            SensitiveTrieCache mockCache = Mockito.mock(SensitiveTrieCache.class);
            mockedStaticSpringBeanUtils.when(() -> SpringBeanUtils.getBean(SensitiveTrieCache.class))
                    .thenReturn(mockCache);
            mockedStaticSpringBeanUtils.when(() -> SpringBeanUtils.getProperty(anyString(), anyString()))
                    .thenReturn("0");
            SensitiveTrie inputTrie = new SensitiveTrie();
            inputTrie.insert("天选打工人", SensitiveTrie.SensitiveOp.REPLY, "输入兜底回复");
            SensitiveTrie outputTrie = new SensitiveTrie();
            outputTrie.insert("打工人", SensitiveTrie.SensitiveOp.REPLACE, "xxx");
            outputTrie.insert("米饭", SensitiveTrie.SensitiveOp.FILTER, null);
            outputTrie.insert("天选打工人", SensitiveTrie.SensitiveOp.REPLY, "输出兜底回复");
            AgentMetadata metadata = AgentMetadata.builder().isContentReviewEnable(true).build();
            SensitiveTrieCache.SensitiveTrieWrapper trieWrapper =
                    new SensitiveTrieCache.SensitiveTrieWrapper(true, 0, inputTrie, outputTrie);
            Mockito.when(mockCache.getSensitiveTrieWrapper(anyString(), any())).thenReturn(trieWrapper);
            sensitiveTrieUtils = new SensitiveTrieUtils("agent", metadata);

            mockSecurityService = Mockito.mock(SecurityService.class);
            mockedStaticSpringBeanUtils.when(() -> SpringBeanUtils.getBean(SecurityService.class))
                    .thenReturn(mockSecurityService);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testHandleSensitiveMatch() throws Exception {
        // 输入兜底匹配
        String text = "我是天选打工人";
        Pair<Integer, String> matchResult = sensitiveTrieUtils.handleSensitiveMatch(text, true);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLY.getCode(), matchResult.getLeft());
        Assertions.assertEquals("输入兜底回复", matchResult.getRight());
        // 输出兜底匹配
        matchResult = sensitiveTrieUtils.handleSensitiveMatch(text, false);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLY.getCode(), matchResult.getLeft());
        Assertions.assertEquals("输出兜底回复", matchResult.getRight());
        // 输出替换
        text = "我是打工人";
        matchResult = sensitiveTrieUtils.handleSensitiveMatch(text, false);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLACE.getCode(), matchResult.getLeft());
        Assertions.assertEquals("我是xxx", matchResult.getRight());
        // 输出过滤
        text = "我爱吃米饭";
        matchResult = sensitiveTrieUtils.handleSensitiveMatch(text, false);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.FILTER.getCode(), matchResult.getLeft());
        Assertions.assertEquals("我爱吃", matchResult.getRight());
    }

    @Test
    public void testHandleOutputSensitiveMatch() throws Exception {
        String identifier = "mock";
        SensitiveTrieUtils.MatchResultWrapper wrapper =
                sensitiveTrieUtils.handleOutputSensitiveMatch("你好", identifier, false);
        // 未匹配敏感词，op=0
        Assertions.assertEquals(0, wrapper.getOp());
        Assertions.assertEquals("你好", wrapper.getText());
        // 未匹配敏感词，op=0, 敏感词前缀“打”不返回
        wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("我是打", identifier, false);
        Assertions.assertEquals(0, wrapper.getOp());
        Assertions.assertEquals("我是", wrapper.getText());
        // 触发替换匹配，wrapper.getOffset() 表示之前已完成匹配的部分
        wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("工人的", identifier, false);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLACE.getCode(), wrapper.getOp());
        Assertions.assertEquals("xxx的", wrapper.getText());
        Assertions.assertEquals("你好我是".length(), wrapper.getOffset());
        // 触发兜底回复，需全量替换
        wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("还是天选打工人", identifier, false);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLY.getCode(), wrapper.getOp());
        Assertions.assertTrue(sensitiveTrieUtils.isMatchingWithId(identifier));
        // 触发兜底回复，isFinished为true，返回全量替换操作（offset=0）
        wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("", identifier, true);
        Assertions.assertEquals(SensitiveTrie.SensitiveOp.REPLY.getCode() | SensitiveTrie.SensitiveOp.APPEND.getCode(),
                wrapper.getOp());
        Assertions.assertEquals("输出兜底回复", wrapper.getText());
        Assertions.assertEquals(0, wrapper.getOffset());
        Assertions.assertFalse(sensitiveTrieUtils.isMatchingWithId(identifier));
    }

    @Test
    public void handleSensitiveMatchTest() {
        ReflectionTestUtils.setField(sensitiveTrieUtils, "isContentReviewEnable", false);
        ReflectionTestUtils.setField(sensitiveTrieUtils, "isSafetyBarrierEnabled", true);
        ReflectionTestUtils.setField(sensitiveTrieUtils, "securityService", mockSecurityService);

        Pair<Integer, String> ans = sensitiveTrieUtils.handleSensitiveMatch("", true);
        Assertions.assertEquals(0, ans.getLeft());
        Assertions.assertEquals("", ans.getRight());
        ans = sensitiveTrieUtils.handleSensitiveMatch("test", true);
        // When content review is disabled, returns no match (op=0)
        Assertions.assertEquals(0, ans.getLeft());
        Assertions.assertEquals("test", ans.getRight());
    }

    @Test
    public void matchTest() {
        Assertions.assertTrue(sensitiveTrieUtils.isInputMatchEnabled());
        Assertions.assertTrue(sensitiveTrieUtils.isOutputMatchEnabled());
    }

    @Test
    public void handleOutputSensitiveMatchTest() {
        ReflectionTestUtils.setField(sensitiveTrieUtils, "isContentReviewEnable", false);
        ReflectionTestUtils.setField(sensitiveTrieUtils, "isSafetyBarrierEnabled", true);
        ReflectionTestUtils.setField(sensitiveTrieUtils, "securityService", mockSecurityService);

        // When content review is disabled, no match
        SensitiveTrieUtils.MatchResultWrapper wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("test", "msg",
                true);
        Assertions.assertEquals(0, wrapper.getOp());
        Assertions.assertEquals("test", wrapper.getText());

        wrapper = sensitiveTrieUtils.handleOutputSensitiveMatch("test", "msg", false);
        Assertions.assertEquals(0, wrapper.getOp());
        Assertions.assertEquals("test", wrapper.getText());
    }
}