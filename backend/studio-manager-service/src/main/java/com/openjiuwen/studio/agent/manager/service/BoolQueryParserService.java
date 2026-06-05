/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.constant.BoolQueryConstants;
import com.openjiuwen.studio.agent.manager.enums.BoolQueryNodeType;
import com.openjiuwen.studio.agent.manager.enums.BoolQueryOperatorType;
import com.openjiuwen.studio.agent.manager.rce.models.knowledge.BoolQueryNode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 布尔查询解析服务
 *
 * 将布尔查询树结构转换为Elasticsearch查询字符串
 */
public class BoolQueryParserService {

    /**
     * 构建ES查询字符串
     *
     * @param node 布尔查询节点
     * @return ES查询字符串
     */
    public static String buildEsQuery(BoolQueryNode node) {
        if (node == null) {
            return "";
        }
        return parseNode(node);
    }

    /**
     * 递归解析节点
     *
     * @param node 布尔查询节点
     * @return 解析后的查询字符串
     */
    private static String parseNode(BoolQueryNode node) {
        // === 1. 叶子节点 (RULE) ===
        if (BoolQueryNodeType.RULE.equals(node.getType())) {
            // 不带双引号，仅做转义
            return escapeLucene(node.getValue());
        }

        // === 2. 分支节点 (GROUP) ===
        if (BoolQueryNodeType.GROUP.equals(node.getType())) {
            List<BoolQueryNode> children = node.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                return "";
            }

            // 递归解析所有子节点，得到字符串列表
            List<String> childQueries = children.stream()
                    .map(BoolQueryParserService::parseNode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(childQueries)) return "";

            BoolQueryOperatorType operator = node.getOperator() != null ?
                    node.getOperator() : BoolQueryOperatorType.AND;

            if (BoolQueryOperatorType.NOT.equals(operator)) {
                // NOT后只有一个节点，直接取第一个
                return String.format(BoolQueryConstants.NOT_OPERATOR_TEMPLATE, childQueries.get(0));
            }

            // 标准处理 AND / OR
            // 结果格式：(充电知识 OR 车辆软件功能)
            return String.format(BoolQueryConstants.GROUP_OPERATOR_TEMPLATE,
                    String.join(BoolQueryConstants.SPACE + operator.getValue() + BoolQueryConstants.SPACE,
                            childQueries));
        }
        return "";
    }

    /**
     * 转义 Lucene 特殊字符
     *
     * @param value 待转义的字符串
     * @return 转义后的字符串
     */
    private static String escapeLucene(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 仅转义特殊符号
        String specialChars = BoolQueryConstants.LUCENE_SPECIAL_CHARS;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
