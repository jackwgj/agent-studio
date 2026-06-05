/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.service;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.rce.client.ModelManagerClient;
import com.openjiuwen.studio.agent.common.rce.model.ModelAssetInfo;
import com.openjiuwen.studio.agent.common.rce.model.ModelServiceInfo;
import com.openjiuwen.studio.agent.runtime.rce.client.DataManagerClient;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowModel;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowVo;
import com.openjiuwen.studio.agent.runtime.rce.model.CreateBackflowReq;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 功能描述 数据工程接口服务
 *
 */
@Slf4j
@Service
public class DataManagerService {
    private final DataManagerClient dataManagerClient;

    private final ModelManagerClient modelManagerClient;

    public DataManagerService(DataManagerClient dataManagerClient, ModelManagerClient modelManagerClient) {
        this.dataManagerClient = dataManagerClient;
        this.modelManagerClient = modelManagerClient;
    }

    /**
     * 创建数据回流任务
     *
     * @param req req
     */
    public void createDataBackflowJob(String projectId, CreateBackflowReq req) {
        try {
            // 根据请求中的model_deployment_id信息，获取完整的模型信息
            String modelDeploymentId = req.getData().get(0).getModel().getDeploymentId();
            if (modelDeploymentId == null) {
                return;
            }
            ResponseEntity<ModelServiceInfo> modelResponse =
                modelManagerClient.queryModelInfo(RequestContextUtils.getRequestAuthToken(), modelDeploymentId);
            if (!modelResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Get model info from model manager server error! response:{}", modelResponse);
                return;
            }
            ModelServiceInfo serviceInfo = modelResponse.getBody();
            if (Objects.isNull(serviceInfo)) {
                log.error("Get model info is null");
                return;
            }

            // 解析模型信息，获取model_version，model_name，更新req中的model信息
            List<ModelAssetInfo> modelAssetInfos = serviceInfo.getAssets();
            if (modelAssetInfos == null || modelAssetInfos.isEmpty()) {
                log.error("Get model asset info is null");
                return;
            }
            String modelVersion = modelAssetInfos.get(0).getExternalVersion();
            String modelName = serviceInfo.getServiceName();
            BackflowVo backflowVo = req.getData().get(0);
            backflowVo.setModel(new BackflowModel(modelDeploymentId, modelName, modelVersion));
            log.info("Get model info succeed, model deployment id: {}", modelDeploymentId);

            ResponseEntity<JSONObject> response = dataManagerClient.createBackflowJob(
                RequestContextUtils.getRequestAuthToken(), projectId, serviceInfo.getWorkspaceId(), req);
            log.info("Create data backflow job result: code [{}] body [{}]", response.getStatusCode(),
                response.getBody());
        } catch (Exception e) {
            log.error("create feedback data backflow job failed, {}", e.getMessage());
        }
    }
}
