/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection;

import com.openjiuwen.studio.agent.foundation.connection.AbstractKnowledgeBaseConnector;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorFactory;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.lakeserach.LakeSearchConnector;
import com.openjiuwen.studio.agent.foundation.connection.ragflow.RagFlowConnector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ConnectorFactoryTest {
    private AutoCloseable mockitoCloseable;

    @Mock
    private ConnectorClient connectorClient;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getKnowledgeBaseConnectorInstance_should_return_lakeSearch_when_condition() throws Exception {
        // Given
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder().code("LakeSearch").build();

        // When
        AbstractKnowledgeBaseConnector result = ConnectorFactory.getKnowledgeBaseConnectorInstance(connectorDefinition,
            connectorClient);

        // Then
        Assertions.assertInstanceOf(LakeSearchConnector.class, result);
    }

    @Test
    void test_getKnowledgeBaseConnectorInstance_should_return_ragflow_when_condition() throws Exception {
        // Given
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder().code("Ragflow").build();

        // When
        AbstractKnowledgeBaseConnector result = ConnectorFactory.getKnowledgeBaseConnectorInstance(connectorDefinition,
            connectorClient);

        // Then
        Assertions.assertInstanceOf(RagFlowConnector.class, result);
    }

    @Test
    void test_getKnowledgeBaseConnectorInstance_should_exception_when_connector_is_not_support() throws Exception {

        // Given
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder().code("Nothing").build();

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> ConnectorFactory.getKnowledgeBaseConnectorInstance(connectorDefinition, connectorClient));
    }
}