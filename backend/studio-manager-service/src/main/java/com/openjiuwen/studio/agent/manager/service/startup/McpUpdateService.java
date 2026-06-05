/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.startup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerTool;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.enums.VisibilityEnum;
import com.openjiuwen.studio.agent.manager.mapper.McpServerConfigMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.McpJsonUtils;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class McpUpdateService {

    @Autowired
    private McpServerMapper mcpServerMapper;

    @Autowired
    private McpServerConfigMapper mcpServerConfigMapper;

    @Autowired
    private McpServiceMapper mcpServiceMapper;

    @Autowired
    private EncryptionAdapter encryptionAdapter;

    public void updateMcp() {
        List<McpServerInfo> mcpServerOld = mcpServerMapper.selectAll();
        log.info("mcpServerOld sum = {}", mcpServerOld.size());
        List<McpServerConfig> confOld = mcpServerConfigMapper.selectAll();
        List<McpServiceEntity> target = convertOld2McpService(mcpServerOld, confOld);
        log.info("target sum = {}", target.size());
        List<String> curIds = mcpServiceMapper.selectByIds(target.stream().map(McpServiceEntity::getId).toList())
            .stream()
            .map(McpServiceEntity::getId)
            .toList();
        log.info("curIds = {}", curIds.size());
        target.removeIf(mcpServiceEntity -> curIds.contains(mcpServiceEntity.getId()));
        log.info("target = {}", target.size());
        if (!CollectionUtils.isEmpty(target)) {
            mcpServiceMapper.batchInsert(target);
        }
    }

    private List<McpServiceEntity> convertOld2McpService(List<McpServerInfo> mcpServerOld,
        List<McpServerConfig> configs) {
        List<McpServiceEntity> target = new ArrayList<>();
        for (McpServerInfo server : mcpServerOld) {
            McpServerConfig cf = configs.stream()
                .filter(config -> server.getServerId().equals(config.getServerId()))
                .findFirst()
                .orElse(new McpServerConfig());
            McpServiceEntity mcpServiceEntity = new McpServiceEntity();
            mcpServiceEntity.setId(server.getServerId());
            mcpServiceEntity.setTraceId(server.getServerId());
            mcpServiceEntity.setName(server.getName());
            mcpServiceEntity.setNameEn(server.getNameEn());
            mcpServiceEntity.setDescription(server.getDesc());
            mcpServiceEntity.setDescriptionEn(server.getDesc());
            mcpServiceEntity.setReadme(server.getDesc());
            mcpServiceEntity.setOrgType("SSE");
            mcpServiceEntity.setDeployType("sse");
            mcpServiceEntity.setServerConfig(encryptionAdapter.encrypt(buildServerConfig(server, cf.getAuthKeys()), RequestContextUtils.getRequestUserDomainId()));
            mcpServiceEntity.setFcInstanceUrl(server.getUrl());
            mcpServiceEntity.setFcInstanceStatus("success");
            mcpServiceEntity.setServerId("1");
            mcpServiceEntity.setCreatedDate(new Timestamp(server.getCreateTime().getTime()));
            mcpServiceEntity.setLastUpdatedDate(new Timestamp(server.getUpdateTime().getTime()));
            mcpServiceEntity.setUniqueCode("");
            mcpServiceEntity.setProjectId(server.getProjectId());
            mcpServiceEntity.setTenantId("0");
            mcpServiceEntity.setWorkspaceId(server.getProjectId());
            mcpServiceEntity.setIcon(server.getIcon());
            if (!Strings.CS.equals(server.getVisibility(), "global") || !Strings.CS.equals(server.getVisibility(),
                "user") || !Strings.CS.equals(server.getVisibility(), "workspace")) {
                mcpServiceEntity.setVisibility(VisibilityEnum.WORKSPACE.getValue());
            } else {
                mcpServiceEntity.setVisibility(server.getVisibility());
            }
            mcpServiceEntity.setNodeType("Mcp");
            mcpServiceEntity.setCreatedByUserId(server.getCreatorId());
            mcpServiceEntity.setLastUpdatedByUserId(server.getCreatorId());
            mcpServiceEntity.setDeleted(false);
            mcpServiceEntity.setDeptCode(CommonUtil.getDeptCode());
            mcpServiceEntity.setTools(buildTools(server.getTools()));
            target.add(mcpServiceEntity);
        }

        return target;
    }

    private String buildTools(McpServerTools tools) {
        List<McpSchema.Tool> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (McpServerTool tool : tools.getTools()) {
            try {
                // 在这里捕获异常
                McpSchema.Tool tool1 = McpSchema.Tool.builder()
                    .name(tool.getName())
                    .description(tool.getDesc())
                    .inputSchema(objectMapper.readValue(tool.getInput(), McpSchema.JsonSchema.class))
                    .build();
                result.add(tool1);
            } catch (JsonProcessingException e) {
                // 记录错误日志，明确是哪个工具解析失败
                log.error("Failed to parse input schema for tool: {}", tool.getName(), e);
            }
        }
        return McpJsonUtils.toJson(result);
    }

    private String buildServerConfig(McpServerInfo server, List<AuthKeyInfo> authKeys) {
        // 替换参数
        String urlParam = server.getUrl();
        if (ObjectUtils.isNotEmpty(server.getAuth())) {
            if (AuthInfo.DomainEnum.QUERY.equals(server.getAuth().getDomain())) {
                urlParam = urlParam + buildQuery(server.getAuth(), authKeys);
            }
        }
        Map<String, Object> mcpServers = new HashMap<>();
        Map<String, Object> exam = new HashMap<>();
        // 更新 URL
        Map<String, Object> resultMap = new HashMap<>();

        // 更新 JSON
        if (ObjectUtils.isNotEmpty(server.getAuth())) {
            if (AuthInfo.DomainEnum.HEADERS.equals(server.getAuth().getDomain())) {
                // 更新 headers
                Map<String, Object> headers = buildCurHeaders(server.getAuth(), authKeys);
                // 检查并进行类型转换
                exam.put("headers", headers);
            }
        }
        exam.put("url", urlParam);
        mcpServers.put("example-sse", exam);
        resultMap.put("mcpServers", mcpServers);

        // 输出更新后的 JSON
        return McpJsonUtils.toJson(resultMap);
    }

    private Map<String, Object> buildCurHeaders(AuthInfo auth, List<AuthKeyInfo> authKeys) {
        Map<String, Object> headers = new HashMap<>();
        if (ObjectUtils.isNotEmpty(auth.getAuthKeys())) {
            for (AuthKeyInfo authKeyInfo : auth.getAuthKeys()) {
                if (StringUtils.isNotBlank(authKeyInfo.getAuthKey())) {
                    String targetName = authKeyInfo.getTargetName();
                    String authKey = encryptionAdapter.decrypt(authKeyInfo.getAuthKey());

                    // 将提取的值放入 headers JSONObject
                    headers.put(targetName, authKey);
                }
            }
        }

        if (ObjectUtils.isNotEmpty(authKeys)) {
            for (AuthKeyInfo authKeyInfo : authKeys) {
                if (StringUtils.isNotBlank(authKeyInfo.getAuthKey())) {
                    String targetName = authKeyInfo.getTargetName();
                    String authKey = encryptionAdapter.decrypt(authKeyInfo.getAuthKey());

                    // 将提取的值放入 headers JSONObject
                    headers.put(targetName, authKey);
                }
            }
        }

        return headers;
    }

    private String buildQuery(AuthInfo auth, List<AuthKeyInfo> authKeys) {
        StringBuilder result = new StringBuilder("?");

        if (auth.getAuthKeys() != null) {
            for (AuthKeyInfo authKeyInfo : auth.getAuthKeys()) {
                if (StringUtils.isNotBlank(authKeyInfo.getAuthKey())) {
                    result.append(authKeyInfo.getTargetName())
                        .append("=")
                        .append(encryptionAdapter.decrypt(authKeyInfo.getAuthKey()))
                        .append("&");
                }
            }
        }

        if (authKeys != null) {
            for (AuthKeyInfo authKeyInfo : authKeys) {
                if (StringUtils.isNotBlank(authKeyInfo.getAuthKey())) {
                    result.append(authKeyInfo.getTargetName())
                        .append("=")
                        .append(encryptionAdapter.decrypt(authKeyInfo.getAuthKey()))
                        .append("&");
                }
            }
        }

        return result.substring(0, result.length() - 1);
    }
}
