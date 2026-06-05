/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * 逻辑类型枚举
 *
 */
public enum OperatorType {
    /**
     * 相等
     */
    EQUAL("eq", "(%s == %s)"),
    /**
     * 不相等
     */
    NOT_EQUAL("not_eq", "(%s != %s)"),
    /**
     * 包含
     */
    CONTAIN("contain", "(%s in %s)", true),
    /**
     * 不包含
     */
    NOT_CONTAIN("not_contain", "(%s not_in %s)", true),
    /**
     * 空
     */
    IS_EMPTY("is_empty", "(is_empty(%s))"),
    /**
     * 非空
     */
    IS_NOT_EMPTY("is_not_empty", "(is_not_empty(%s))"),
    /**
     * 长度大于
     */
    LONGER_THAN("longer_than", "(%s > %s)"),
    /**
     * 长度大于等于
     */
    LONGER_THAN_OR_EQUAL("longer_than_or_eq", "(%s >= %s)"),
    /**
     * 长度小于
     */
    SHORTER_THAN("shorter_than", "(%s < %s)"),
    /**
     * 长度小于等于
     */
    SHORTER_THAN_OR_EQUAL("shorter_than_or_eq", "(%s <= %s)");

    /**
     * EI逻辑类型
     */
    private final String eiType;

    /**
     * 是否左右表达式反转
     */
    private final boolean isReverse;

    /**
     * jiuWen逻辑类型
     */
    private final String template;

    OperatorType(String eiType, String template) {
        this.eiType = eiType;
        this.template = template;
        this.isReverse = false;
    }

    OperatorType(String eiType, String template, boolean isReverse) {
        this.eiType = eiType;
        this.isReverse = isReverse;
        this.template = template;
    }

    /**
     * 返回EI类型
     *
     * @return 返回EI类型
     */
    public String getEiType() {
        return String.valueOf(eiType);
    }

    /**
     * 返回模板类型
     *
     * @return 返回模板类型
     */
    public String getTemplate() {
        return String.valueOf(template);
    }

    /**
     * 返回是否反转
     *
     * @return 返回是否反转
     */
    public boolean isReverse() {
        return isReverse;
    }
}
