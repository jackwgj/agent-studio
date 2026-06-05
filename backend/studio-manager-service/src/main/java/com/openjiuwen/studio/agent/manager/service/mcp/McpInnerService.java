/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.enums.VisibilityEnum;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class McpInnerService implements com.openjiuwen.studio.agent.manager.service.mcp.IMcpInnerService {

    @Autowired
    private EncryptionAdapter encryptionAdapter;

    @Autowired
    private McpServiceMapper mcpServiceMapper;

    @Autowired
    private ShareInnerService shareInnerService;

    @Override
    public List<McpServiceEntity> getMcpByIds(String workspaceId, List<String> validMcpServerIds) {
        if (CollectionUtils.isEmpty(validMcpServerIds)) {
            return new ArrayList<>();
        }
        return mcpServiceMapper.selectList(validMcpServerIds, null, null, workspaceId);
    }

    @Override
    public List<McpServerInfo> getMcpServerInfoByIds(String workspaceId, List<String> validMcpServerIds) {
        if (CollectionUtils.isEmpty(validMcpServerIds)) {
            return new ArrayList<>();
        }
        List<McpServiceEntity> mcpServer = mcpServiceMapper.selectList(validMcpServerIds, null, null, workspaceId);
        if (CollectionUtils.isEmpty(mcpServer)) {
            return new ArrayList<>();
        }
        return mcpServer.stream().map(this::buildMcpServerInfo).toList();
    }

    @Override
    public McpServerInfo getMcpServerInfoById(String id, String targetWorkspaceId) {
        McpServiceEntity mcpServer = mcpServiceMapper.selectByPrimaryKey(id, targetWorkspaceId);
        if (ObjectUtils.isEmpty(mcpServer)) {
            return null;
        }
        return buildMcpServerInfo(mcpServer);
    }

    @Override
    public McpServiceEntity getMcpByName(String workspaceId, String name) {
        List<McpServiceEntity> mcpServiceEntitys = mcpServiceMapper.selectByName(name, null, null, workspaceId);
        if (CollectionUtils.isNotEmpty(mcpServiceEntitys)) {
            return mcpServiceEntitys.get(0);
        }
        throw new AgentStudioException(StudioError.MCP_ADAPT_IR_FAILED, name);
    }

    private McpServerInfo buildMcpServerInfo(McpServiceEntity mcpServer) {
        McpServerInfo mcpServerInfo = new McpServerInfo();
        mcpServerInfo.setServerId(mcpServer.getId());
        mcpServerInfo.setProjectId(mcpServer.getProjectId());
        mcpServerInfo.setWorkspaceId(mcpServer.getWorkspaceId());
        mcpServerInfo.setName(mcpServer.getName());
        mcpServerInfo.setNameEn(mcpServer.getNameEn());
        mcpServerInfo.setDesc(mcpServer.getDescription());
        mcpServerInfo.setIcon(mcpServer.getIcon());
        mcpServerInfo.setVisibility(mcpServer.getVisibility());
        mcpServerInfo.setUrl(mcpServer.getFcInstanceUrl());
        mcpServerInfo.setAuth(null);
        mcpServerInfo.setType(mcpServer.getNodeType());
        mcpServerInfo.setCategory("public");
        mcpServerInfo.setConfigStatus("none");
        mcpServerInfo.setBinding(true);
        mcpServerInfo.setCreator(mcpServerInfo.getCreator());
        mcpServerInfo.setCreatorId(mcpServerInfo.getCreatorId());
        mcpServerInfo.setCreateTime(mcpServer.getCreatedDate());
        mcpServerInfo.setUpdateTime(mcpServer.getLastUpdatedDate());
        mcpServerInfo.setServiceConfig(mcpServer.getServerConfig());
        mcpServerInfo.setMcpTool(mcpServer.getTools());
        mcpServerInfo.setOrigin(mcpServer.getOrigin());
        mcpServerInfo.setOrgType(mcpServer.getOrgType());
        mcpServerInfo.setDeployType(mcpServer.getDeployType());
        mcpServerInfo.setTraceId(mcpServer.getTraceId());
        return mcpServerInfo;
    }

    /**
     * 判断当前用户下是否存在该MCP server
     *
     * @param workspaceId workspaceId
     * @param name MCP name
     * @return Boolean
     */
    @Override
    public boolean isMcpExist(String workspaceId, String name) {
        List<McpServiceEntity> mcpServiceEntitys = mcpServiceMapper.selectByName(name, null, null, workspaceId);
        return !CollectionUtils.isEmpty(mcpServiceEntitys);
    }

    @Override
    public boolean createOrUpdate(McpServerInfo server, String targetWorkspaceId) {
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServiceEntity.setId(server.getServerId());
        mcpServiceEntity.setTraceId(server.getServerId());
        mcpServiceEntity.setName(server.getName());
        mcpServiceEntity.setNameEn(server.getNameEn());
        mcpServiceEntity.setDescription(server.getDesc());
        mcpServiceEntity.setDescriptionEn(server.getDesc());
        mcpServiceEntity.setReadme(server.getDesc());
        mcpServiceEntity.setOrgType(server.getOrgType());
        mcpServiceEntity.setDeployType(server.getDeployType());
        mcpServiceEntity.setServerConfig(buildServerConfig(server.getServiceConfig()));
        mcpServiceEntity.setFcInstanceUrl(server.getUrl());
        mcpServiceEntity.setFcInstanceStatus("success");
        mcpServiceEntity.setServerId("1");
        mcpServiceEntity.setCreatedDate(new Timestamp(server.getCreateTime().getTime()));
        mcpServiceEntity.setLastUpdatedDate(new Timestamp(server.getUpdateTime().getTime()));
        mcpServiceEntity.setUniqueCode("");
        mcpServiceEntity.setProjectId(server.getProjectId());
        mcpServiceEntity.setTenantId("0");
        mcpServiceEntity.setWorkspaceId(targetWorkspaceId);
        mcpServiceEntity.setIcon(server.getIcon());
        if (!Strings.CS.equals(server.getVisibility(), "global") || !Strings.CS.equals(server.getVisibility(), "user")
            || !Strings.CS.equals(server.getVisibility(), "workspace")) {
            mcpServiceEntity.setVisibility(VisibilityEnum.WORKSPACE.getValue());
        } else {
            mcpServiceEntity.setVisibility(server.getVisibility());
        }
        mcpServiceEntity.setNodeType("Mcp");
        mcpServiceEntity.setCreatedByUserId(server.getCreatorId());
        mcpServiceEntity.setLastUpdatedByUserId(server.getCreatorId());
        mcpServiceEntity.setDeleted(false);
        mcpServiceEntity.setDeptCode(CommonUtil.getDeptCode());
        mcpServiceEntity.setTools(server.getMcpTool());
        mcpServiceEntity.setTraceId(server.getTraceId());
        try {
            if (isMcpExist(targetWorkspaceId, server.getName())) {
                mcpServiceMapper.updateById(server.getServerId(), mcpServiceEntity);
            } else {
                mcpServiceMapper.insert(mcpServiceEntity);
            }
        } catch (Exception e) {
            log.error(String.format("Failed to import MCP server: %s", server.getName()), e);
            return false;
        }
        return true;
    }

    @Override
    public String buildServerConfig(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        // 获取当前租户ID
        String currentDomainId = RequestContextUtils.getRequestUserDomainId();

        // 1. 尝试解密
        // 如果 text 是 SCC 密文 -> NodeAdapter 走 SCC 解密 -> 返回明文 -> 不相等 -> 返回 text (正确)
        // 如果 text 是 KMS 密文 -> NodeAdapter 走 KMS 解密 -> 返回明文 -> 不相等 -> 返回 text (正确)
        // 如果 text 是 明文     -> NodeAdapter 解密失败(返回原文) -> 相等 -> 往下走加密 (正确)
        String decrypted = encryptionAdapter.decrypt(text);

        if (!Strings.CS.equals(text, decrypted)) {
            return text;
        }

        // 2. 加密
        return encryptionAdapter.encrypt(text, currentDomainId);
    }
}
