/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.common.dto.agent.ListWorkflowsQo;
import com.openjiuwen.studio.agent.common.dto.md.ModelServiceInvokeData;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.ImportRsp;
import com.openjiuwen.studio.agent.runtime.dto.KmsDatakeyResponse;
import com.openjiuwen.studio.agent.common.dto.run.WorkflowListRsp;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceListRsp;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseInst;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseReportReq;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;
import java.util.Map;

@FeignClient(name = "agentManager", url = "${feign.client.config.agentManager.url:}")
public interface AgentManagerClient {
    @PostMapping(value = "/v1/{project_id}/agent-manager/import-agents", consumes = "multipart/form-data")
    ResponseEntity<ImportRsp> importAgents(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestPart("file") Resource file,
        @RequestParam(value = "import_agents") String importAgents,
        @RequestParam(value = "import_workflows") String importWorkflows,
        @RequestParam(value = "import_tools") String importTools);

    @PostMapping(value = "/v1/{project_id}/agent-manager/import-workflows", consumes = "multipart/form-data")
    ResponseEntity<ImportRsp> importWorkflows(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestPart("file") Resource file,
        @RequestParam(value = "import_workflows") String importWorkflows,
        @RequestParam(value = "import_tools") String importTools);

    @PostMapping(value = "/v1/{project_id}/agent-manager/import-tools", consumes = "multipart/form-data")
    ResponseEntity<ImportRsp> importTools(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestPart("file") Resource file,
        @RequestParam(value = "import_ids") String importTools);

    @GetMapping(value = "/v1/{project_id}/agent-manager/workflows", consumes = "application/json")
    ResponseEntity<WorkflowListRsp> listWorkflows(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @SpringQueryMap ListWorkflowsQo listWorkflowsQo);

    @GetMapping( {"/v1/common-intern/license/query"})
    ResponseEntity<List<LicenseInst>> queryLicense(
        @RequestParam(value = "resId", required = false) @Valid @Size(max = 64) String resId,
        @RequestParam(value = "skuCode", required = false) @Valid @Size(max = 64) String skuCode,
        @RequestParam(value = "attrCode", required = false) @Valid @Size(max = 64) String attrCode,
        @RequestHeader(defaultValue = Constant.Common.X_AUTH_TOKEN, required = false) String token,
        @RequestHeader(value = "domain-id", required = false) String domainId);

    @PostMapping( {"/v1/common-intern/license/report"})
    ResponseEntity<String> report(@RequestBody @Valid LicenseReportReq req,
        @RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @RequestHeader(value = "domain-id", required = false) String domainId);

    @GetMapping("/v1/{project_id}/model-manager/query/model-invoke/data")
    ResponseEntity<ModelServiceInvokeData> queryModelInvokeData(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestParam("model_id") String modelId,
        @RequestParam("workspace_id") String workspaceId);

    @GetMapping("/v1/{project_id}/model-manager/available/model-services")
    ResponseEntity<ModelServiceListRsp> queryAvailableModels(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestParam("workspace_id") String workspaceId,
        @RequestParam("id") String modelId);

    @PostMapping("/v1/{project_id}/model-manager/model-test/status")
    ResponseEntity<Void> updateModelStatus(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @PathVariable(value = "project_id") String projectId, @RequestParam("workspace_id") String workspaceId,
        @RequestBody Map<String, String> body);

    /**
     * 通过GET方法获取指定域的KMS数据密钥信息
     *
     * @param token 用户的认证令牌，通过HTTP请求头"X-Auth-Token"获取
     * @param domainId 指定的域ID，通过URL路径参数获取
     * @return 返回一个包含KMS数据密钥信息的响应实体
     */
    @GetMapping("/v1/common-intern/kms/data-key/{domain_id}")
    ResponseEntity<KmsDatakeyResponse> queryKmsInfo(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
                                                    @PathVariable("domain_id") String domainId);

    /**
     * 通过GET方法获取指定域的KMS数据密钥信息
     *
     * @param token 用户的认证令牌，通过HTTP请求头"X-Auth-Token"获取
     * @param keyId 加密秘钥KeyId
     * @return 返回一个包含KMS数据密钥信息的响应实体
     */
    @GetMapping("/v1/common-intern/kms/data-key/queryKmsInfoByKey/{key_id}")
    ResponseEntity<KmsDatakeyResponse> queryKmsInfoByKey(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
                                                         @PathVariable("key_id") String keyId);
}
