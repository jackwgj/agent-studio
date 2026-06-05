/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt;

import com.openjiuwen.studio.agent.manager.dto.WorkflowInfo;

import java.util.Map;

/**
 * 适配器服务接口类
 *
 */
public interface AdapterService {

    /**
     * 解析第三方工作流信息并转换为WorkflowInfo格式
     *
     * @param thirdpartyWorkflowInfo
     * @return WorkflowInfo
     */
    WorkflowInfo convert(Map<String, Object> thirdpartyWorkflowInfo);


    /**
     * 校验格式
     *
     * @param thirdpartyWorkflowInfo
     */
    void validateFormat(Map<String, Object> thirdpartyWorkflowInfo);

}
