/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.VariableVo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.validation.annotation.Validated;

/**
 * 变量信息
 *
 */
@Data
@Accessors(chain = true)
@Builder
@Validated
@ExcelTarget("变量信息")
public class VariableExport {
    /**
     * 变量key
     */
    @Excel(name = "excel.export.variable.key", width = 30.0d)
    private String variableKey;

    /**
     * 变量值
     */
    @Excel(name = "excel.export.variable.value", width = 100.0d)
    private String variableValue;

    public static VariableExport convertToVariableExport(VariableVo variableVo) {
        VariableVoToVariableExportConverter toVariableExportConverter
            = new VariableVoToVariableExportConverter();
        return toVariableExportConverter.convert(variableVo);
    }

    private static class VariableVoToVariableExportConverter implements FormConvert<VariableVo, VariableExport> {
        @Override
        public VariableExport convert(VariableVo variableVo) {
            return VariableExport.builder()
                .variableKey(variableVo.getKey())
                .variableValue(variableVo.getValue())
                .build();
        }
    }
}
