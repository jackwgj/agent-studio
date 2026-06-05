/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.SqlKeywordEnum;
import com.openjiuwen.studio.agent.common.enums.SqlOperatorType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowDataQueryConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 数据库节点转换IR
 *
 */
public class SqlNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 数据源id
     */
    public static final String ID = "id";

    /**
     * sql语句
     */
    public static final String SQL = "sql";

    /**
     * 数据查询条件
     */
    public static final String SELECT_QUERY = "select_query";

    /**
     * 查询数据库字段列表
     */
    public static final String OUTPUT_COLUMN = "output_column";

    /**
     * 查询数据库表名
     */
    public static final String TABLE_NAME = "table_name";

    /**
     * 查询模板SQL
     */
    public static final String SELECT_TEMPLATE = "SELECT %s FROM %s";

    /**
     * 查询条件值
     */
    public static final String CONDITIONS = "conditions";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        configs.put(ID, nodeConfigs.get(ID));
        if (nodeConfigs.get(SELECT_QUERY) != null) {
            if (nodeConfigs.get(OUTPUT_COLUMN) == null) {
                throw new AgentStudioException(StudioError.DATASOURCE_SELECT_COLUMN_NOT_NULL);
            }
            String selectColumn = constructColumnByList(JSONArray.from(nodeConfigs.get(OUTPUT_COLUMN)));
            List<String> conditions = new ArrayList<>();
            WorkflowDataQueryConfigVO workflowDataQueryConfigVO =
                    JSON.parseObject(JSON.toJSONString(nodeConfigs.get(SELECT_QUERY), JSONWriter.Feature.WriteMapNullValue),
                            WorkflowDataQueryConfigVO.class);
            String sql = String.format(SELECT_TEMPLATE, selectColumn, nodeConfigs.get(TABLE_NAME)) + adaptExpression(workflowDataQueryConfigVO, conditions);
            configs.put(SQL, sql);
            configs.put(CONDITIONS, conditions);
        } else {
            configs.put(SQL, nodeConfigs.get(SQL));
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.SQL.getIrType();
    }

    public String adaptExpression(WorkflowDataQueryConfigVO dataQueryConfig, List<String> conditions) {
        StringBuilder sqlExpression = new StringBuilder();
        if (dataQueryConfig.getConditions() != null && !dataQueryConfig.getConditions().isEmpty()) {
            String operator = dataQueryConfig.getLogic().toString();
            List<String> expressions = dataQueryConfig.getConditions()
                    .stream()
                    .map(m -> adaptExpression(
                            "`" + m.getLeft().getName() + "`",
                            m.getRight() != null
                                    ? adaptExpressionValue(m.getRight().getValue(), m.getRight().getType()) : null,
                            m.getOperator(), conditions))
                    .toList();
            String conditionExpression = String.join(" " + SqlKeywordEnum.valueOf(operator) + " ", expressions);
            sqlExpression.append(String.format(SqlKeywordEnum.WHERE.getValue())).append(conditionExpression);
        }
        if (dataQueryConfig.getOrders() != null && !dataQueryConfig.getOrders().isEmpty()) {
            List<String> orderExpression = dataQueryConfig.getOrders()
                    .stream()
                    .map(m -> {
                        String column = "`" + m.getField() + "`";
                        return m.isAsc() ? column + SqlKeywordEnum.ASC.getValue() : column + SqlKeywordEnum.DESC.getValue();
                    }).toList();
            sqlExpression.append(String.format(SqlKeywordEnum.ORDER_BY.getValue())).append(String.join(", ", orderExpression));
        }
        sqlExpression.append(String.format(SqlKeywordEnum.LIMIT.getValue())).append(dataQueryConfig.getLimit());
        sqlExpression.append(String.format(SqlKeywordEnum.OFFSET.getValue())).append(dataQueryConfig.getOffset());
        return sqlExpression.toString();
    }

    private String adaptExpression(String column, String input, String operator, List<String> conditions) {
        if (input == null && !Strings.CI.equals(operator, SqlOperatorType.IS_NULL.getEiType()) && !Strings.CI.equals(operator, SqlOperatorType.IS_NOT_NULL.getEiType())) {
            throw new AgentStudioException(StudioError.WORKFLOW_UNEXPECTED_OPERATOR, operator);
        }
        for (SqlOperatorType type : SqlOperatorType.values()) {
            if (Strings.CS.equals(operator, type.getEiType())) {
                return formatExpression(column, input, type, conditions);
            }
        }
        throw new AgentStudioException(StudioError.WORKFLOW_UNEXPECTED_OPERATOR, operator);
    }

    private String adaptExpressionValue(WorkflowFieldVOValue workflowFieldValue, String fieldType) {
        String valueType = WorkflowUtils.formatWorkflowType(fieldType);
        if (workflowFieldValue.getType().equals(WorkflowFieldVOValue.TypeEnum.REF)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> refMap =
                    objectMapper.convertValue(workflowFieldValue.getContent(), new TypeReference<>() {});
            return String.format("{{%s}}", refMap.get(CommonConstant.Workflow.REF_VAR_NAME));
        } else if (workflowFieldValue.getType().equals(WorkflowFieldVOValue.TypeEnum.LITERAL)) {
            TypeEnum typeEnum = TypeEnum.fromValue(valueType);
            if (typeEnum == TypeEnum.ARRAY) {
                return JSONArray.from(workflowFieldValue.getContent()).toJSONString();
            } else {
                return StringEscapeUtils.escapeSql(workflowFieldValue.getContent().toString());
            }
        } else {
            throw new AgentStudioException(StudioError.WORKFLOW_BRANCH_FIELD_VALUE);
        }
    }

    private String formatExpression(String left, String right, SqlOperatorType type, List<String> conditions) {
        switch (type) {
            case IN, NOT_IN -> {
                if (right.startsWith("{{")) {
                    return String.format(type.getTemplate(), left, right);
                }
                String replaceRight = constructValueByList(JSON.parseArray(right), conditions);
                return String.format(type.getTemplate(), left, replaceRight);
            }
            case LIKE, NOT_LIKE -> {
                conditions.add("%" + right + "%");
                return String.format(type.getTemplate(), left);
            }
            case IS_NULL, IS_NOT_NULL -> {
                return String.format(type.getTemplate(), left);
            }
            default -> {
                conditions.add(right);
                return String.format(type.getTemplate(), left);
            }
        }
    }

    private String constructValueByList(List<?> contents, List<String> conditions) {
        if (contents == null || contents.isEmpty()) {
            return SqlKeywordEnum.NULL.getValue();
        }
        StringJoiner joiner = new StringJoiner(", ");
        contents.forEach(element -> {
            joiner.add("?");
            conditions.add(element.toString());
        });
        return "(" + joiner + ")";
    }

    private String constructColumnByList(List<?> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new AgentStudioException(StudioError.DATASOURCE_SELECT_COLUMN_NOT_NULL);
        }
        StringJoiner joiner = new StringJoiner(", ");
        columns.forEach(element -> {
            String column = "`" + element.toString() + "`";
            joiner.add(String.format(SqlKeywordEnum.LEFT.getValue(), column, Integer.parseInt(IRAdapter.irAdapterConfig.getDatasourceQueryMaxLength()), column));
        });
        return joiner.toString();
    }
}
