/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DynamicPromptVarExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    // 单个Sheet的解析结果：内层List=行数据，最内层=列/变量
    private final List<List<PromptVar>> rowDataList = new ArrayList<>();

    // 表头缓存：列索引 → 变量名（varKey）
    private Map<Integer, String> headerMap;


    /**
     * 解析表头（第一行）
     */
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headerMap = new HashMap<>();
        // 遍历表头，过滤空值，存储「列索引→变量名」
        headMap.forEach((index, cellData) -> {
            String varKey = cellData.getStringValue();
            if (varKey != null && !varKey.isBlank()) {
                headerMap.put(index, varKey.trim());
            }
        });
        // 表头为空则抛出异常
        if (headerMap.isEmpty()) {
            throw new IllegalArgumentException("Excel第一个Sheet的表头为空，无有效变量名");
        }
    }

    /**
     * 解析数据行（第二行及以后）
     */
    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        if (headerMap == null || headerMap.isEmpty()) {
            return;
        }
        // 单行数据转换为PromptVar列表
        List<PromptVar> rowPromptVars = new ArrayList<>();
        rowData.forEach((index, cellValue) -> {
            String varKey = headerMap.get(index);
            if (varKey == null || varKey.isBlank()) {
                return; // 跳过无变量名的列
            }
            // 构建PromptVar
            PromptVar promptVar = new PromptVar();
            promptVar.setName(varKey);
            promptVar.setContent(cellValue == null ? "" : cellValue.trim());
            rowPromptVars.add(promptVar);
        });
        // 非空行才加入结果
        if (!rowPromptVars.isEmpty()) {
            rowDataList.add(rowPromptVars);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
