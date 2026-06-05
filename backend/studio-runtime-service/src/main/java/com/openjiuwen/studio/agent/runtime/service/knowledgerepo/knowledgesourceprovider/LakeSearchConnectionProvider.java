/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.common.enums.RagAuthMode;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.LakeSearchConnection;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.LsBasicAuth;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class LakeSearchConnectionProvider extends KnowledgeSourceConnectionProvider {

    private static final String ENDPOINT = "endpoint";

    private static final String OCR_ENABLE = "ocr_enable";

    private static final String AUTH_MODE = "auth_mode";

    private static final String AUTHORIZATION = "authorization";

    protected LakeSearchConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        super(knowledgeConnectionMapper);
    }

    @Override
    public LakeSearchConnection getKnowledgeSourceConnection(String connectionId) {
        try {

            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = queryKnowledgeConnectionFromDb(connectionId);
            return convertToLakeSearchConnection(knowledgeBaseConnectionEntity);
        } catch (Exception exception) {
            log.error("Fail to get KnowledgeBase Connection Info:{}", exception.getMessage());
            throw new AgentStudioException(StudioError.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
    }

    public LakeSearchConnection convertToLakeSearchConnection(
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        List<KnowledgeBaseConnectionParam> params = knowledgeBaseConnectionEntity.getParams();
        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setConnectionId(knowledgeBaseConnectionEntity.getId());
        RagAuthMode ragAuthMode = null;
        // 识别auth_mode和通用配置项
        for (KnowledgeBaseConnectionParam connectorParamDefinition : params) {
            switch (connectorParamDefinition.getCode()) {
                case ENDPOINT -> lakeSearchConnection.setEndpoint(connectorParamDefinition.getValue());
                case OCR_ENABLE ->
                    lakeSearchConnection.setOcrEnable(Boolean.parseBoolean(connectorParamDefinition.getValue()));
                case AUTH_MODE -> {
                    ragAuthMode = RagAuthMode.valueOf(connectorParamDefinition.getValue().toUpperCase(Locale.ENGLISH));
                    lakeSearchConnection.setAuthMode(ragAuthMode.toString());
                }
            }
        }
        switch (Objects.requireNonNull(ragAuthMode)) {
            case NONE -> {
            }
            case BASIC -> {
                LsBasicAuth lsBasicAuth = new LsBasicAuth();
                for (KnowledgeBaseConnectionParam connectorParamDefinition : params) {
                    if (connectorParamDefinition.getCode().equals(AUTHORIZATION)) {
                        lsBasicAuth.setLakeSearchAuthorization(connectorParamDefinition.getValue());
                    }
                }
                lakeSearchConnection.setLsBasicAuth(lsBasicAuth);
            }
            default -> {
                log.error("unknown lakesearch auth mode[{}]", ragAuthMode);
                throw new AgentStudioException(StudioError.LAKE_SEARCH_AUTH_INVALID);
            }
        }
        lakeSearchConnection.isValid();
        return lakeSearchConnection;
    }
}
