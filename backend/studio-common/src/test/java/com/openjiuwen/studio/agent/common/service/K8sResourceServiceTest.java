/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.service.K8sResourceService;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.util.Config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MockitoSettings(strictness = Strictness.LENIENT)
class K8sResourceServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Set<String> appLabels;

    @InjectMocks
    private K8sResourceService k8sService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(k8sService, "appLabels", appLabels);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getAgentResourceCpu_should_equal_result() throws Exception {
        // Given
        when(appLabels.contains(anyString())).thenReturn(true);
        CoreV1Api api = mock(CoreV1Api.class);
        CoreV1Api.APIlistPodForAllNamespacesRequest request =
                mock(CoreV1Api.APIlistPodForAllNamespacesRequest.class);
        when(api.listPodForAllNamespaces()).thenReturn(request);
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName("pod");
        objectMeta.setLabels(Map.of("group", "agent-builder-agent"));
        V1PodSpec podSpec = new V1PodSpec();
        List<V1Container> containers = new ArrayList<>();
        podSpec.setContainers(containers);
        V1Container container = new V1Container();
        containers.add(container);
        V1ResourceRequirements requirements = new V1ResourceRequirements();
        container.setResources(requirements);
        Quantity quantity = new Quantity("2");
        requirements.setLimits(Map.of("cpu", quantity));
        V1Pod pod = new V1Pod();
        pod.setMetadata(objectMeta);
        pod.setSpec(podSpec);
        V1PodList podList = new V1PodList();
        podList.setItems(List.of(pod));
        when(request.execute()).thenReturn(podList);
        // When
        double result = k8sService.getAgentResourceCpu(api);

        // Then
        assertEquals(2, result);
    }

    @Test
    void test_getAgentResourceCpu_should_equal_result1() {
        try (MockedStatic<Config> mockedStaticConfig = mockStatic(Config.class, RETURNS_DEEP_STUBS)) {
            // Given
            ApiClient client = new ApiClient();
            mockedStaticConfig.when(Config::fromCluster).thenReturn(client);

            when(appLabels.contains(anyString())).thenReturn(true);

            // When
            double result = k8sService.getAgentResourceCpu();

            // Then
            assertEquals(0.0d, result);
        }
    }

    @Test
    void test_getAgentResourceCpu_should_equal_result2() {
        try (MockedStatic<Config> mockedStaticConfig = mockStatic(Config.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticConfig.when(Config::fromCluster).thenReturn(null);

            when(appLabels.contains(anyString())).thenReturn(true);

            // When
            double result = k8sService.getAgentResourceCpu();

            // Then
            assertEquals(0.0d, result);
        }
    }

    @Test
    void test_getAgentResourceCpu1_should_throw_exception() {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(appLabels.contains(anyString())).thenReturn(true);
            CoreV1Api api = mock(CoreV1Api.class);
            // When
            k8sService.getAgentResourceCpu(api);

        });
    }
}
