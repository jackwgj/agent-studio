/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums.ThirdpartyWorkflowType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 第三方工作流适配器：获取实现类的Bean
 *
 */
@Slf4j
@Service
public class ThirdpartyWorkflowAdapter {

    @Autowired
    private Map<String, AdapterService> adapterServiceMap;

    /**
     * 获取实现服务类
     *
     * @param type 工作流文件类型
     * @return
     */
    public AdapterService getService(String type) {

        // 空值判断
        if (StringUtils.isEmpty(type)) {
            log.error("The thirdparty workflow type is empty!");
            throw new AgentStudioException(StudioError.PARSE_THIRD_PARTY_WORKFLOW_FILE_FAILED);
        }

        // 校验类型是否支持
        ThirdpartyWorkflowType thirdpartyWorkflowType = ThirdpartyWorkflowType.fromValue(type);
        if (ObjectUtils.isEmpty(thirdpartyWorkflowType)) {
            log.error("Unsupport thirdparty workflow type!");
            throw new AgentStudioException(StudioError.PARSE_THIRD_PARTY_WORKFLOW_FILE_FAILED);
        }

        // 获取AdapterService服务类
        String serviceName = thirdpartyWorkflowType.getType() + "AdapterService";
        AdapterService adapterService = adapterServiceMap.get(serviceName);

        // 返回
        return adapterService;
    }
}
