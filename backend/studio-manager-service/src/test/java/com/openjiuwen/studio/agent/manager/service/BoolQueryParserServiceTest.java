/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openjiuwen.studio.agent.manager.enums.BoolQueryNodeType;
import com.openjiuwen.studio.agent.manager.enums.BoolQueryOperatorType;
import com.openjiuwen.studio.agent.manager.rce.models.knowledge.BoolQueryNode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test for BoolQueryParserService.buildEsQuery
 * Focusing on BoolQueryNode parsing logic
 */
class BoolQueryParserServiceTest {

    @Test
    @DisplayName("Should correctly parse complex nested boolean logic (AND, OR, NOT)")
    void testComplexNestedBooleanLogic() {
        // 构建复杂的 BoolQueryNode 结构: ((充电知识 OR 车辆软件功能) AND (NOT 智界))
        BoolQueryNode group1 = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.OR)
                .children(Arrays.asList(
                        createRuleNode("充电知识"),
                        createRuleNode("车辆软件功能")
                ))
                .build();

        BoolQueryNode group2 = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.NOT)
                .children(Collections.singletonList(createRuleNode("智界")))
                .build();

        BoolQueryNode rootGroup = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.AND)
                .children(Arrays.asList(group1, group2))
                .build();

        // 2. Execute
        String result = BoolQueryParserService.buildEsQuery(rootGroup);

        // 3. Verify
        String expected = "((充电知识 OR 车辆软件功能) AND (NOT 智界))";
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should correctly parse single RULE node")
    void testSingleRuleNode() {
        BoolQueryNode ruleNode = createRuleNode("充电知识");
        String result = BoolQueryParserService.buildEsQuery(ruleNode);
        assertEquals("充电知识", result);
    }

    @Test
    @DisplayName("Should correctly parse simple OR operation")
    void testSimpleOrOperation() {
        BoolQueryNode orNode = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.OR)
                .children(Arrays.asList(
                        createRuleNode("tagA"),
                        createRuleNode("tagB")
                ))
                .build();

        String result = BoolQueryParserService.buildEsQuery(orNode);
        assertEquals("(tagA OR tagB)", result);
    }

    @Test
    @DisplayName("Should correctly parse simple AND operation")
    void testSimpleAndOperation() {
        BoolQueryNode andNode = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.AND)
                .children(Arrays.asList(
                        createRuleNode("tagA"),
                        createRuleNode("tagB")
                ))
                .build();

        String result = BoolQueryParserService.buildEsQuery(andNode);
        assertEquals("(tagA AND tagB)", result);
    }

    @Test
    @DisplayName("Should correctly parse NOT operation")
    void testNotOperation() {
        BoolQueryNode notNode = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.NOT)
                .children(Collections.singletonList(createRuleNode("智界")))
                .build();

        String result = BoolQueryParserService.buildEsQuery(notNode);
        assertEquals("(NOT 智界)", result);
    }

    @Test
    @DisplayName("Should return empty string for node with no children")
    void testEmptyGroupNode() {
        BoolQueryNode emptyGroup = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.AND)
                .children(Collections.emptyList())
                .build();

        String result = BoolQueryParserService.buildEsQuery(emptyGroup);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should ignore empty child results")
    void testIgnoreEmptyChildren() {
        BoolQueryNode groupWithEmptyChildren = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.AND)
                .children(Arrays.asList(
                        createRuleNode("validTag"),
                        createRuleNode(""),  // Empty string should be filtered
                        createRuleNode("   ")  // Whitespace only should be filtered
                ))
                .build();

        String result = BoolQueryParserService.buildEsQuery(groupWithEmptyChildren);
        assertEquals("(validTag)", result);
    }

    @Test
    @DisplayName("Should escape Lucene special characters in RULE values")
    void testLuceneEscape() {
        BoolQueryNode ruleNode = createRuleNode("v1.0+debug/test");
        String result = BoolQueryParserService.buildEsQuery(ruleNode);
        assertEquals("v1.0\\+debug\\/test", result);
    }

    @Test
    @DisplayName("Should return empty string for null node")
    void testNullNode() {
        String result = BoolQueryParserService.buildEsQuery(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should use default AND operator when operator is null")
    void testNullOperator() {
        BoolQueryNode nullOperatorNode = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(null)  // Should default to AND
                .children(Arrays.asList(
                        createRuleNode("tagA"),
                        createRuleNode("tagB")
                ))
                .build();

        String result = BoolQueryParserService.buildEsQuery(nullOperatorNode);
        assertEquals("(tagA AND tagB)", result);
    }

    @Test
    @DisplayName("Should handle nested complex structure")
    void testNestedComplexStructure() {
        // 构建结构: (A AND (B OR C) AND (NOT D))
        BoolQueryNode orGroup = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.OR)
                .children(Arrays.asList(
                        createRuleNode("B"),
                        createRuleNode("C")
                ))
                .build();

        BoolQueryNode notGroup = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.NOT)
                .children(Collections.singletonList(createRuleNode("D")))
                .build();

        BoolQueryNode rootGroup = BoolQueryNode.builder()
                .type(BoolQueryNodeType.GROUP)
                .operator(BoolQueryOperatorType.AND)
                .children(Arrays.asList(
                        createRuleNode("A"),
                        orGroup,
                        notGroup
                ))
                .build();

        String result = BoolQueryParserService.buildEsQuery(rootGroup);
        assertEquals("(A AND (B OR C) AND (NOT D))", result);
    }

    // --- Helper Methods ---

    private BoolQueryNode createRuleNode(String value) {
        return BoolQueryNode.builder()
                .type(BoolQueryNodeType.RULE)
                .value(value)
                .build();
    }
}
