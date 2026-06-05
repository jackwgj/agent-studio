/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.config;

import com.openjiuwen.studio.agent.space.api.vo.response.config.FrontConfigInfo;
import com.openjiuwen.studio.agent.space.api.vo.response.config.KooPageEnvConfigInfo;
import com.openjiuwen.studio.agent.space.app.config.KooPageEnvConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigApiService {
    @Value("${agent.apsce.frontConfig.agentStudioUrl:}")
    private String agentStudioUrl;

    /**
     * 获取当前用户信息
     */
    public FrontConfigInfo getFrontConfig() {
        log.info("getFrontConfig start");
        KooPageEnvConfigInfo kooPageEnvConfigInfo = KooPageEnvConfigInfo.builder()
            .aiUrl(KooPageEnvConfiguration.getInstance().getAiUrl())
            .apiUrl(KooPageEnvConfiguration.getInstance().getApiUrl())
            .editorUrl(KooPageEnvConfiguration.getInstance().getEditorUrl())
            .collaborationUrl(KooPageEnvConfiguration.getInstance().getCollaborationUrl())
            .multiTableCollaborationUrl(KooPageEnvConfiguration.getInstance().getMultiTableCollaborationUrl())
            .flowDiagramUrl(KooPageEnvConfiguration.getInstance().getFlowDiagramUrl())
            .monacoEditorUrl(KooPageEnvConfiguration.getInstance().getMonacoEditorUrl())
            .dolphinWebUrl(KooPageEnvConfiguration.getInstance().getDolphinWebUrl())
            .obsPrefix(KooPageEnvConfiguration.getInstance().getObsPrefix())
            .DataAccessURL(KooPageEnvConfiguration.getInstance().getDataAccessURL())
            .appId(KooPageEnvConfiguration.getInstance().getAppId())
            .build();
        return FrontConfigInfo.builder()
            .agentStudioUrl(agentStudioUrl)
            .kooPageEnvConfigInfo(kooPageEnvConfigInfo)
            .build();
    }
}
