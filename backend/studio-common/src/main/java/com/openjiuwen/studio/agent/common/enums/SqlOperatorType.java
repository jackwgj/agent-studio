/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

public enum SqlOperatorType {

    /**
     * 相等
     */
    EQUAL("EQUAL", "(%s = ?)"),
    /**
     * 不相等
     */
    NOT_EQUAL("NOT_EQUAL", "(%s != ?)"),
    /**
     * 空
     */
    IS_NULL("IS_NULL", "(%s IS NULL)"),
    /**
     * 非空
     */
    IS_NOT_NULL("IS_NOT_NULL", "(%s IS NOT NULL)"),
    /**
     * 大于
     */
    LONGER_THAN("LONGER_THAN", "(%s > ?)"),
    /**
     * 大于等于
     */
    LONGER_THAN_OR_EQUAL("LONGER_THAN_OR_EQUAL", "(%s >= ?)"),
    /**
     * 小于
     */
    SHORTER_THAN("SHORTER_THAN", "(%s < ?)"),
    /**
     * 小于等于
     */
    SHORTER_THAN_OR_EQUAL("SHORTER_THAN_OR_EQUAL", "(%s <= ?)"),
    /**
     * 属于
     */
    IN("IN", "(%s IN %s)"),
    /**
     * 不属于
     */
    NOT_IN("NOT_IN", "(%s NOT IN %s)"),
    /**
     * 模糊匹配
     */
    LIKE("LIKE", "(%s LIKE ?)"),
    /**
     * 模糊不匹配
     */
    NOT_LIKE("NOT_LIKE", "(%s NOT LIKE ?)");

    /**
     * EI逻辑类型
     */
    private final String eiType;

    /**
     * jiuWen逻辑类型
     */
    private final String template;

    SqlOperatorType(String eiType, String template) {
        this.eiType = eiType;
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
}
