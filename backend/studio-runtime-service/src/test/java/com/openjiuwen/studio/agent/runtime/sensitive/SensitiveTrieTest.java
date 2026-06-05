/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
public class SensitiveTrieTest {
    private SensitiveTrie sensitiveTrie;

    @BeforeEach
    public void setUp() throws Exception {
        sensitiveTrie = new SensitiveTrie();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void test_match_text() throws Exception {
        String text = "你好，我是小张，代号Ab, 我吃完了，我喜欢吃饭，还喜欢吃菜";
        sensitiveTrie.insert("AB", SensitiveTrie.SensitiveOp.REPLACE, "007");
        sensitiveTrie.insert("完了", SensitiveTrie.SensitiveOp.REPLACE, "面包");
        sensitiveTrie.insert("小张", SensitiveTrie.SensitiveOp.FILTER, "无用");
        sensitiveTrie.insert("你好", SensitiveTrie.SensitiveOp.REPLACE, "您好");
        sensitiveTrie.insert("吃饭", SensitiveTrie.SensitiveOp.REPLACE, "恰饭");
        sensitiveTrie.insert("吃菜", SensitiveTrie.SensitiveOp.REPLACE, "恰菜");
        // run the test
        Pair<Integer, String> result = sensitiveTrie.match(text);
        // verify the results
        assertEquals("您好，我是，代号007, 我吃面包，我喜欢恰饭，还喜欢恰菜", result.getRight());

        sensitiveTrie.insert("代号ab", SensitiveTrie.SensitiveOp.REPLY, "我是兜底回复");
        result = sensitiveTrie.match(text);
        // verify the results
        assertEquals("我是兜底回复", result.getRight());
    }

    @Test
    public void test_match_stream_text() throws Exception {
        final List<String> streamText =
            List.of("你好", "我是小", "张", "我今天1", "5岁", "我上初中了", "我喜欢打", "篮球", "还喜欢踢足", "球", "你呢？", "真", "的", "吗？", "不知道");
        sensitiveTrie.insert("你好", SensitiveTrie.SensitiveOp.REPLACE, "您好");
        sensitiveTrie.insert("今天", SensitiveTrie.SensitiveOp.REPLACE, "今年");
        sensitiveTrie.insert("上初中", SensitiveTrie.SensitiveOp.FILTER, "无用");
        sensitiveTrie.insert("打篮球", SensitiveTrie.SensitiveOp.REPLACE, "羽毛球");
        sensitiveTrie.insert("足球", SensitiveTrie.SensitiveOp.FILTER, "无用");
        sensitiveTrie.insert("呢？", SensitiveTrie.SensitiveOp.REPLACE, "喜欢吗？");
        sensitiveTrie.insert("真的吗？", SensitiveTrie.SensitiveOp.REPLACE, "请回答");
        sensitiveTrie.insert("小张", SensitiveTrie.SensitiveOp.REPLACE, "大明");
        sensitiveTrie.insert("知道", SensitiveTrie.SensitiveOp.REPLACE, "错");

        List<String> expected =
            List.of("您好", "我是", "大明", "我今年1", "5岁", "我了", "我喜欢", "羽毛球", "还喜欢踢", "你喜欢吗？", "请回答", "不错");

        StringBuilder lastBuilder = new StringBuilder();
        List<String> results = new ArrayList<>();
        for (String chunk : streamText) {
            Pair<Integer, String> res = sensitiveTrie.matchStream(chunk, lastBuilder);
            if (StringUtils.isNotBlank(res.getRight())) {
                results.add(res.getRight());
            }
        }
        // verify the results
        assertEquals(expected, results);
    }
}
