/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider;

import com.openjiuwen.studio.agent.common.dto.knowledge.KooSearchAuthMode;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.KooSearchConnection;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

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

    protected KooSearchConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        super(knowledgeConnectionMapper);
    }

    @Override
    public KooSearchConnection getKnowledgeSourceConnection(String connectionId) {
        // 此处应查询数据库获取知识源连接信息
        try {
            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = queryKnowledgeConnectionFromDb(connectionId);
            return convertToKooSearchConnection(knowledgeBaseConnectionEntity);
        } catch (Exception exception) {
            log.error("Fail to get KnowledgeBase Connection Info", exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
    }

    private KooSearchConnection convertToKooSearchConnection(
        @NotNull KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        List<KnowledgeBaseConnectionParam> knowledgeBaseConnectionParams = knowledgeBaseConnectionEntity.getParams();
        KooSearchConnection kooSearchConnection = new KooSearchConnection();
        KooSearchAuthMode kooSearchAuthMode = null;
        kooSearchConnection.setConnectionId(knowledgeBaseConnectionEntity.getId());
        // 识别auth_mode和通用配置项
        for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
            switch (knowledgeBaseConnectionParam.getCode()) {
                case ENDPOINT -> kooSearchConnection.setEndpoint(knowledgeBaseConnectionParam.getValue());
                case OCR_ENABLE ->
                    kooSearchConnection.setOcrEnable(Boolean.parseBoolean(knowledgeBaseConnectionParam.getValue()));
                case APPLICATION_ID -> kooSearchConnection.setApplicationId(knowledgeBaseConnectionParam.getValue());
                case PROJECT_ID -> kooSearchConnection.setProjectId(knowledgeBaseConnectionParam.getValue());
                case APP_CODE -> kooSearchConnection.setAppCode(knowledgeBaseConnectionParam.getValue());
                case AUTH_MODE -> {
                    kooSearchAuthMode = KooSearchAuthMode.valueOf(
                        knowledgeBaseConnectionParam.getValue().toUpperCase(Locale.ENGLISH));
                    kooSearchConnection.setAuthMode(kooSearchAuthMode.getAuthMode());
                }
            }
        }

        switch (Objects.requireNonNull(kooSearchAuthMode)) {
            case NONE -> {
            }
            case TOKEN -> {
                for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
                    switch (knowledgeBaseConnectionParam.getCode()) {
                        case PROJECT_NAME ->
                            kooSearchConnection.setProjectName(knowledgeBaseConnectionParam.getValue());
                        case DOMAIN_NAME -> kooSearchConnection.setDomainName(knowledgeBaseConnectionParam.getValue());
                        case USER_NAME -> kooSearchConnection.setUserName(knowledgeBaseConnectionParam.getValue());
                        case USER_PASSWORD ->
                            kooSearchConnection.setUserPassword(knowledgeBaseConnectionParam.getValue());
                    }
                }
            }
            case APP_CODE -> {
                for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
                    if (knowledgeBaseConnectionParam.getCode().equals(APP_CODE)) {
                        kooSearchConnection.setAppCode(knowledgeBaseConnectionParam.getValue());
                    }
                }
            }
            default -> {
                log.error("unknown koosearch auth mode[{0}]", kooSearchAuthMode);
                throw new AgentBaseException(ErrorCode.KOO_SEARCH_AUTH_MODE_ERROR);
            }
        }
        kooSearchConnection.isValid();
        return kooSearchConnection;
    }

    public String getKooSearchAuthMode(String connectionId) {
        KooSearchConnection kooSearchConnection = getKnowledgeSourceConnection(connectionId);
        return kooSearchConnection.getAuthMode();
    }

    public String getConnectionIdBySegmentId(String segmentId) {
        return getKnowledgeSourceConnectionBySegmentId(segmentId).getId();
    }

}
