/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider;


import com.openjiuwen.studio.agent.common.dto.knowledge.KooSearchAuthMode;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.KooSearchConnection;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class KooSearchConnectionProvider extends KnowledgeSourceConnectionProvider {

    private static final String AUTH_MODE = "auth_mode";

    private static final String ENDPOINT = "endpoint";

    private static final String APPLICATION_ID = "application_id";

    private static final String USER_NAME = "user_name";

    private static final String USER_PASSWORD = "user_password";

    private static final String PROJECT_ID = "project_id";

    private static final String PROJECT_NAME = "project_name";

    private static final String APP_CODE = "app_code";

    private static final String DOMAIN_NAME = "domain_name";

    private static final String OCR_ENABLE = "ocr_enable";

    public KooSearchConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        super(knowledgeConnectionMapper);
    }

    @Override
    public KooSearchConnection getKnowledgeSourceConnection(String connectionId) {
        // 此处应查询数据库获取知识源连接信息
        try {
            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = queryKnowledgeConnectionFromDb(connectionId);
            return convertToKooSearchConnection(knowledgeBaseConnectionEntity);
        } catch (Exception exception) {
            log.error("Fail to get KnowledgeBase Connection Info, message:{}, exception:{}", exception.getMessage(),
                exception);
            throw new AgentStudioException(StudioError.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
    }

    private KooSearchConnection convertToKooSearchConnection(
        @NotNull KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        List<KnowledgeBaseConnectionParam> params = knowledgeBaseConnectionEntity.getParams();
        KooSearchConnection kooSearchConnection = new KooSearchConnection();
        KooSearchAuthMode kooSearchAuthMode = null;
        kooSearchConnection.setConnectionId(knowledgeBaseConnectionEntity.getId());
        // 识别auth_mode和通用配置项
        for (KnowledgeBaseConnectionParam connectorParamDefinition : params) {
            switch (connectorParamDefinition.getCode()) {
                case ENDPOINT -> kooSearchConnection.setEndpoint(connectorParamDefinition.getValue());
                case OCR_ENABLE ->
                    kooSearchConnection.setOcrEnable(Boolean.parseBoolean(connectorParamDefinition.getValue()));
                case APPLICATION_ID -> kooSearchConnection.setApplicationId(connectorParamDefinition.getValue());
                case PROJECT_ID -> kooSearchConnection.setProjectId(connectorParamDefinition.getValue());
                case APP_CODE -> kooSearchConnection.setAppCode(connectorParamDefinition.getValue());
                case AUTH_MODE -> {
                    kooSearchAuthMode = KooSearchAuthMode.valueOf(
                        connectorParamDefinition.getValue().toUpperCase(Locale.ENGLISH));
                    kooSearchConnection.setAuthMode(kooSearchAuthMode.getAuthMode());
                }
                default -> log.warn("unknown koosearch connection param: [{}]", connectorParamDefinition.getCode());
            }
        }

        switch (Objects.requireNonNull(kooSearchAuthMode)) {
            case NONE -> {
            }
            case TOKEN -> {
                for (KnowledgeBaseConnectionParam connectorParamDefinition : params) {
                    switch (connectorParamDefinition.getCode()) {
                        case PROJECT_NAME -> kooSearchConnection.setProjectName(connectorParamDefinition.getValue());
                        case DOMAIN_NAME -> kooSearchConnection.setDomainName(connectorParamDefinition.getValue());
                        case USER_NAME -> kooSearchConnection.setUserName(connectorParamDefinition.getValue());
                        case USER_PASSWORD -> kooSearchConnection.setUserPassword(connectorParamDefinition.getValue());
                        default ->
                            log.warn("unknown koosearch connection param: [{}]", connectorParamDefinition.getCode());
                    }
                }
            }
            case APP_CODE -> {
                for (KnowledgeBaseConnectionParam connectorParamDefinition : params) {
                    if (connectorParamDefinition.getCode().equals(APP_CODE)) {
                        kooSearchConnection.setAppCode(connectorParamDefinition.getValue());
                    }
                }
            }
            default -> {
                log.error("unknown koosearch auth mode: [{}]", kooSearchAuthMode);
                throw new AgentStudioException(StudioError.KOO_SEARCH_AUTH_MODE_ERROR);
            }
        }
        kooSearchConnection.isValid();
        return kooSearchConnection;
    }

}