/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MockitoSettings(strictness = Strictness.LENIENT)
class K8sServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Set<String> appLabels;

    @InjectMocks
    private K8sService k8sService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
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
        CoreV1Api api = Mockito.mock(CoreV1Api.class);
        CoreV1Api.APIlistPodForAllNamespacesRequest request =
            Mockito.mock(CoreV1Api.APIlistPodForAllNamespacesRequest.class);
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
}
