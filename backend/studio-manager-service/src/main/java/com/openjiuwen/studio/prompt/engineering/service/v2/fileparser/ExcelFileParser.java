/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.alibaba.excel.EasyExcel;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.listener.DynamicPromptVarExcelListener;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExcelFileParser extends DataSetFileParser {

    @Value("${prompt.evaldata.vars.num: 20}")
    private int varsNum;

    @Value("${prompt.evaldata.vars.namelength: 50}")
    private int varsNameLength;

    @Value("${prompt.evaldata.vars.contentlength: 2000}")
    private int varsContentLength;

    @Override
    public String type() {
        return "xlsx;xls";
    }

    @Override
    public List<List<PromptVar>> parseData(MultipartFile file, Map<String, PromptVar> varsConfig, String projectId,
        String workspaceId, String taskId, int restNum) {

        List<List<PromptVar>> promptVarsList = new ArrayList<>();
        try {
            // 初始化监听器（仅解析第一个Sheet）
            DynamicPromptVarExcelListener listener = new DynamicPromptVarExcelListener();

            // 核心：仅读取第一个Sheet（索引0）
            EasyExcel.read(file.getInputStream(), listener).sheet(0) // 固定读取第一个Sheet（索引从0开始）
                .headRowNumber(1) // 表头在第一行
                .doRead();

            // 返回第一个Sheet的解析结果
            promptVarsList = listener.getRowDataList();

        } catch (Exception e) {
            log.info("excel parse failed!");
            throw new AgentStudioException(StudioError.EXCEL_FILE_FORMAT_INVALID);
        }
        // 校验提示词变量
        validPromptVarsList(promptVarsList, varsConfig, varsNum, varsContentLength, varsNameLength,
            restNum - promptVarsList.size());
        // 去除无关变量
        promptVarsList = clearInvalidVars(promptVarsList, varsConfig);
        return promptVarsList;
    }

}
