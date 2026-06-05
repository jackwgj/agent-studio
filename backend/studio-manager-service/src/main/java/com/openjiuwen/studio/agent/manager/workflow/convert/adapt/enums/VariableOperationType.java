/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums;

import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;

/**
 * 变量赋值节点的操作类型转换
 */
public enum VariableOperationType {

    OVER_WRITE("over-write", WorkflowFieldVOValue.TypeEnum.REF.toString()),

    SET("set", WorkflowFieldVOValue.TypeEnum.LITERAL.toString()),

    CLEAR("clear", WorkflowFieldVOValue.OperatorEnum.EMPTY.toString()),

    /**
     * 以下几种类型暂时不支持
     */
    INCREMENT("+=", null),

    DECREMENT("-=", null),

    MULTIPLY("*=", null),

    DIVIDE("/=", null),

    APPEND("append", null),

    EXTEND("extend", null),

    REMOVE_FIRST("remove-first", null),

    REMOVE_LAST("remove-last", null),

    UNKNOW("unknow", null);

    private final String difyType;

    private final String convertType;

    VariableOperationType(String difyType, String convertType) {
        this.difyType = difyType;
        this.convertType = convertType;
    }

    // convertType 转换为 TypeEnum
    public WorkflowFieldVOValue.TypeEnum toTypeEnum() {
        return WorkflowFieldVOValue.TypeEnum.fromValue(convertType);
    }

    // convertType 转换为 OperatorEnum
    public WorkflowFieldVOValue.OperatorEnum toOperatorEnum() {
        return WorkflowFieldVOValue.OperatorEnum.fromValue(convertType);
    }

    public static VariableOperationType fromValue(String type) {
        for (VariableOperationType variableOperationType : VariableOperationType.values()) {
            if (variableOperationType.difyType.equalsIgnoreCase(type)) {
                return variableOperationType;
            }
        }
        return UNKNOW;
    }
}
