/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.validation.annotation.Validated;

/**
 * 提示词结果导出列表
 *
 */
@Data
@Accessors(chain = true)
@Builder
@Validated
@ExcelTarget("提示词结果导出列表")
public class PromptEvaluationResultExport {
    /**
     * 提示词名称
     */
    @Excel(name = "excel.export.prompt.name")
    private String promptName;

    /**
     * 提示词文本
     */
    @Excel(name = "excel.export.prompt.content", width = 25.0d)
    private String promptContent;

    /**
     * 生成结果
     */
    @Excel(name = "excel.export.generate.result", width = 75.0d)
    private String generateResult;

    /**
     * 评分结果
     */
    @Excel(name = "excel.export.eval.result")
    private String evalResult;
}
