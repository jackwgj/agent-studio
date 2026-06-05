/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * K8s请求
 *
 */
@Component
@Slf4j
public class K8sService {
    private static final double CPU_DENOMINATOR = 1000.0;

    private final Set<String> appLabels = Set.of("agent-base-runtime", "agent-console", "agent-manager", "agent-ops",
        "agent-service", "agent-tools", "code-interpreter", "jiuwen-runtime", "agent-builder-agent-manager");

    /**
     * 通过 pod 获取 agent 服务使用的 cpu 资源
     *
     * @return agent 占用 cpu 总数
     */
    public double getAgentResourceCpu() {
        // 暂时不统计 k8s 之外的资源
        if (!isRunningInKubernetes()) {
            return 0;
        }
        try {
            ApiClient client = Config.fromCluster();
            client.setVerifyingSsl(false);
            client.setLenientOnJson(true);
            Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api();
            return getAgentResourceCpu(api);
        } catch (Exception e) {
            log.error("Get k8s pods error: ", e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
    }

    /**
     * 获取 agent 服务使用的 cpu 资源
     *
     * @return agent 占用 cpu 总数
     */
    public double getAgentResourceCpu(CoreV1Api api) {
        double cpuRequest = 0.0;
        try {
            V1PodList podList = api.listPodForAllNamespaces().execute();
            List<String> pods = new ArrayList<>();
            List<String> agentPods = new ArrayList<>();
            for (V1Pod pod : podList.getItems()) {
                if (pod.getMetadata() == null || pod.getSpec() == null) {
                    continue;
                }
                String podName = pod.getMetadata().getName();
                Map<String, String> labels = pod.getMetadata().getLabels();
                pods.add(podName);
                if (labels == null) {
                    continue;
                }
                String appLabel = labels.get("app");
                String groupLabel = labels.get("group");
                if (!(appLabel != null && appLabels.contains(appLabel) || ("agent-builder-agent".equals(groupLabel)))) {
                    continue;
                }
                agentPods.add(podName);
                for (var container : pod.getSpec().getContainers()) {
                    var resources = container.getResources();
                    if (resources != null && resources.getLimits() != null) {
                        Quantity cpu = resources.getLimits().get("cpu");
                        if (cpu != null) {
                            cpuRequest += parseCpu(cpu.toSuffixedString());
                        }
                    }
                }
            }
            log.info("[K8s Resources]:All pods:{}", pods);
            log.info("[K8s Resources]:Agent pods:{}", agentPods);
        } catch (ApiException e) {
            log.error("Get k8s pods error: ", e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        return cpuRequest;
    }

    private static double parseCpu(String cpuStr) {
        if (cpuStr.endsWith("m")) {
            return Double.parseDouble(cpuStr.substring(0, cpuStr.length() - 1)) / CPU_DENOMINATOR;
        } else {
            return Double.parseDouble(cpuStr);
        }
    }

    private static boolean isRunningInKubernetes() {
        String host = System.getenv("KUBERNETES_SERVICE_HOST");
        String port = System.getenv("KUBERNETES_SERVICE_PORT");
        return host != null && !host.isEmpty() && port != null && !port.isEmpty();
    }
}
