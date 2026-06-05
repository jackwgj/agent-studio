/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.enums;

public enum TemplateImportEnum {
    TEMPLATE_NAME(0, "模板名称", "templateName", "新增候选模板名称", "template name"),
    CONTENT(1, "模板内容", "templateContent", "新增候选模板内容", "template content"),
    DESCRIPTION(2, "描述", "description", "新增候选模板描述", "template description"),
    TAG(3, "标签", "tag", "问答,分类", "Q&A,classify"),
    INDUSTRY(4, "行业", "industry", "通用", "common"),
    VARIABLE(5, "变量", "variable", "{\"content\":\"xxx\",\"name\":\"get_schedule2\",\"type\":\"TEXT\"}", "common");

    private int index;

    private String column;

    private String columnEn;

    private String sampleValue;

    private String sampleValueEn;

    TemplateImportEnum(int index, String column, String columnEn, String sampleValue, String sampleValueEn) {
        this.index = index;
        this.column = column;
        this.columnEn = columnEn;
        this.sampleValue = sampleValue;
        this.sampleValueEn = sampleValueEn;
    }

    public int getIndex() {
        return index;
    }

    public String getColumn() {
        return column;
    }

    public String getColumnEn() {
        return columnEn;
    }

    public String getSampleValue() {
        return sampleValue;
    }

    public String getSampleValueEn() {
        return sampleValueEn;
    }
}
