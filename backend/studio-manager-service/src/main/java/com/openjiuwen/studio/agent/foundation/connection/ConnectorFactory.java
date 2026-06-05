/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import com.openjiuwen.studio.agent.foundation.connection.constants.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.foundation.connection.general.GeneralConnector;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.koosearch.KooSearchConnector;
import com.openjiuwen.studio.agent.foundation.connection.lakeserach.LakeSearchConnector;
import com.openjiuwen.studio.agent.foundation.connection.ragflow.RagFlowConnector;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 生产第三方知识库连接器的工厂
 *
 * @since 2025-08-22
 */
@Slf4j
public class ConnectorFactory {

    private static final Map<String, AbstractKnowledgeBaseConnector> connectorMap = new HashMap<>();

    public static AbstractKnowledgeBaseConnector getKnowledgeBaseConnectorInstance(
        ConnectorDefinition connectorDefinition, ConnectorClient connectorClient) {
        if (connectorDefinition == null) {
            log.error("There is no available connector definition!");
            throw new IllegalArgumentException("connectorDefinition can not be null.");
        }

        if (connectorMap.containsKey(connectorDefinition.getCode())) {
            return connectorMap.get(connectorDefinition.getCode());
        }
        ConnectorTypeEnum connectorType = ConnectorTypeEnum.fromValue(connectorDefinition.getCode());
        if (Objects.isNull(connectorType)) {
            log.error("Connector type can not be empty!");
            throw new IllegalArgumentException("Not supported connector type.");
        }

        switch (connectorType) {
            case GENERAL -> {
                GeneralConnector generalConnector = new GeneralConnector(connectorClient);
                connectorMap.put(connectorDefinition.getCode(), generalConnector);
                return generalConnector;
            }
            case LAKE_SEARCH -> {
                LakeSearchConnector lakeSearchConnector = new LakeSearchConnector(connectorClient);
                connectorMap.put(connectorDefinition.getCode(), lakeSearchConnector);
                return lakeSearchConnector;
            }
            case KOO_SEARCH -> {
                KooSearchConnector kooSearchConnector = new KooSearchConnector(connectorClient);
                connectorMap.put(connectorDefinition.getCode(), kooSearchConnector);
                return kooSearchConnector;
            }
            case RAG_FLOW -> {
                RagFlowConnector ragFlowConnector = new RagFlowConnector(connectorClient);
                connectorMap.put(connectorDefinition.getCode(), ragFlowConnector);
                return ragFlowConnector;
            }
            default -> {
                log.error("Connector type [{}] not supported!", connectorType);
                throw new IllegalArgumentException("Not supported connector type.");
            }
        }
    }
}
