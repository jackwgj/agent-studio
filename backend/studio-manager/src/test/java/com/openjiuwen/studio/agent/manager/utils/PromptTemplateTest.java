/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.utils.KV;
import com.openjiuwen.studio.agent.common.utils.PromptTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PromptTemplateTest {

    private static final String SIMPLE_TEMPLATE = "欢迎{{name}}，您的年龄是{{age}}岁";
    private PromptTemplate promptTemplate;

    @BeforeEach
    void setUp() {
        promptTemplate = new PromptTemplate(SIMPLE_TEMPLATE);
    }

    // Test template rendering with Map parameters
    @Test
    void format_shouldRenderCorrectly_withMapParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 30);

        String result = promptTemplate.format(params);
        assertEquals("欢迎张三，您的年龄是30岁", result);
    }

    // Test template rendering with KV varargs
    @Test
    void format_shouldRenderCorrectly_withKvVarargs() {
        KV kv1 = new KV("name", "李四");
        KV kv2 = new KV("age", 25);

        String result = promptTemplate.format(kv1, kv2);
        assertEquals("欢迎李四，您的年龄是25岁", result);
    }

    // Test template rendering with KV list
    @Test
    void format_shouldRenderCorrectly_withKvList() {
        List<KV> kvList = Arrays.asList(
                new KV("name", "王五"),
                new KV("age", 40)
        );

        String result = promptTemplate.format(kvList);
        assertEquals("欢迎王五，您的年龄是40岁", result);
    }

    // Test template with conditional statement
    @Test
    void render_shouldProcessConditionals_correctly() {
        String complexTemplate = "{% if age >= 18 %}成年{% else %}未成年{% endif %}";
        PromptTemplate template = new PromptTemplate(complexTemplate);

        Map<String, Object> adultParams = new HashMap<>();
        adultParams.put("age", 20);
        assertEquals("成年", template.format(adultParams));

        Map<String, Object> childParams = new HashMap<>();
        childParams.put("age", 15);
        assertEquals("未成年", template.format(childParams));
    }

    // Test template with loop statement
    @Test
    void render_shouldProcessLoops_correctly() {
        String loopTemplate = "产品列表:{% for product in products %} {{product}}{% endfor %}";
        PromptTemplate template = new PromptTemplate(loopTemplate);

        Map<String, Object> params = new HashMap<>();
        params.put("products", Arrays.asList("手机", "电脑", "平板"));

        String result = template.format(params);
        assertEquals("产品列表: 手机 电脑 平板", result);
    }

    // Test missing parameters should leave template tags
    @Test
    void render_shouldPreserveTags_whenParametersMissing() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "赵六");
        params.put("age", 6);

        String result = promptTemplate.format(params);
        assertEquals("欢迎赵六，您的年龄是6岁", result);
    }
}
