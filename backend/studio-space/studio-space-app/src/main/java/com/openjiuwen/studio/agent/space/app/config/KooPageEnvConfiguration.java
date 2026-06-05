/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.config;

import com.openjiuwen.studio.agent.space.common.utils.SpringContextUtils;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 知识数据集相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.space.koopage.env")
public class KooPageEnvConfiguration {
    private String editorUrl;

    private String aiUrl;

    private String apiUrl;

    private String collaborationUrl;

    private String multiTableCollaborationUrl;

    private String flowDiagramUrl;

    private String monacoEditorUrl;

    private String dolphinWebUrl;

    private String obsPrefix;

    private String DataAccessURL;

    private String appId;

    public static KooPageEnvConfiguration getInstance() {
        return SpringContextUtils.getBean(KooPageEnvConfiguration.class);
    }
}
