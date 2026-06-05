/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ThreadLocalUtils;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolRespContent;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfoList;
import com.openjiuwen.studio.agent.manager.dto.McpServerReq;
import com.openjiuwen.studio.agent.manager.dto.McpServerTool;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.enums.ToolType;
import com.openjiuwen.studio.agent.manager.enums.VisibilityEnum;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerConfigMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.agent.manager.rce.models.McpCallToolRequest;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.IconNameCheckUtils;
import com.openjiuwen.studio.agent.manager.utils.ImageBase64Utils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * MCP 服务管理类
 *
 */
@Service
@Slf4j
public class McpServerManagerService implements IMcpServerManagerService {
    private final McpServerMapper serverMapper;

    private final UrlCheckUtils urlCheckUtils;

    private final MappingMapper mappingMapper;

    private final McpServerConfigMapper configMapper;

    private final WorkflowMapper workflowMapper;

    private final MgObsService obsService;

    private final AgentMapper agentMapper;

    private final AgentRuntimeClient runtimeClient;

    private final MessageSource messageSource;

    private WorkflowCommonService workflowCommonService;

    // 1. 正常的注入 (实例变量)
    @Autowired
    private EncryptionAdapter encryptionAdapter;

    @Value("${mcp.encryption.domain-id:mcp-service}")
    private String mcpDomainId;

    // 2. 定义静态变量 (用于在 static 方法里使用)
    private static EncryptionAdapter staticEncryptionAdapter;

    // 3. 关键点：初始化时，把实例变量赋值给静态变量
    @PostConstruct
    public void init() {
        staticEncryptionAdapter = this.encryptionAdapter;
    }

    @Value("${op.svc.project-id}")
    private String opProjectId;

    @Value("${mcp.default-icon}")
    private String defaultIcon;

    public McpServerManagerService(McpServerMapper mapper, UrlCheckUtils utils, MappingMapper mappingMapper,
        McpServerConfigMapper configMapper, WorkflowMapper workflowMapper, MgObsService obsService,
        AgentMapper agentMapper, AgentRuntimeClient runtimeClient, MessageSource messageSource,
        WorkflowCommonService workflowCommonService) {
        this.serverMapper = mapper;
        this.urlCheckUtils = utils;
        this.mappingMapper = mappingMapper;
        this.configMapper = configMapper;
        this.workflowMapper = workflowMapper;
        this.obsService = obsService;
        this.agentMapper = agentMapper;
        this.runtimeClient = runtimeClient;
        this.messageSource = messageSource;
        this.workflowCommonService = workflowCommonService;
    }

    public static void recoveryAuthInfo(McpServerInfo serverInfo) {
        log.info("Start to recovery auth info for server: {}", serverInfo.getServerId());

        McpServerConfig serverConfig = serverInfo.getConfigs();
        AuthInfo authInfo = serverInfo.getAuth();

        if (serverConfig == null || serverConfig.getAuthKeys() == null || authInfo == null || authInfo.getAuthKeys() == null) {
            log.warn("Server config or auth info is missing, skip recovery. Server ID: {}", serverInfo.getServerId());
            return;
        }

        log.debug("Building config key map for server: {}", serverInfo.getServerId());

        Map<String, String> configKeyMap = new HashMap<>();
        serverConfig.getAuthKeys().forEach(item -> {
            configKeyMap.put(item.getTargetName(), item.getAuthKey());
        });

        log.debug("Updating auth keys for server: {}", serverInfo.getServerId());

        authInfo.getAuthKeys().forEach(item -> {
            if (configKeyMap.containsKey(item.getTargetName())) {
                item.setAuthKey(configKeyMap.get(item.getTargetName()));
            }
        });

        log.info("Auth info recovery completed for server: {}", serverInfo.getServerId());
    }

    public static void recoveryAuthInfo(McpServiceEntity serviceEntity) {
        log.info("Start to recovery auth info for service: {}", serviceEntity.getId());

        if (StringUtils.isBlank(serviceEntity.getServerConfig())) {
            log.warn("Server config is blank, skip recovery. Service ID: {}", serviceEntity.getId());
            return;
        }

        Map<String, String> headerInfo;
        try {
            // 使用 if-else 判空，不抛异常
            if (staticEncryptionAdapter == null) {
                log.error("EncryptionAdapter not initialized yet! Skipping decryption.");
                // 设为空 Map，后续逻辑会检测到空并安全退出
                headerInfo = new HashMap<>();
            } else {
                // 只有非空时才调用方法
                String decrypted = staticEncryptionAdapter.decrypt(serviceEntity.getServerConfig());
                headerInfo = CommonUtil.parseMcpConfigHeaderInfo(decrypted);
            }
        } catch (Exception e) {
            log.error(">>>recoveryAuthInfo decrypt failed!, error: {}", e.getMessage());
            headerInfo = new HashMap<>();
        }

        AuthInfo authInfo = null == serviceEntity.getAuth() ? new AuthInfo() : serviceEntity.getAuth();

        // 刚才如果 adapter 为空，headerInfo 就是空的，这里会直接 return，逻辑是安全的
        if (CollectionUtils.isEmpty(headerInfo)) {
            log.warn("Header info is empty, no auth keys to recover. Service ID: {}", serviceEntity.getId());
            return;
        }

        List<AuthKeyInfo> authKeyInfos = null == authInfo.getAuthKeys() ? new ArrayList<AuthKeyInfo>() : authInfo.getAuthKeys();

        headerInfo.forEach((key, value) -> {
            AuthKeyInfo authKeyInfo = new AuthKeyInfo();
            authKeyInfo.setTargetName(key);
            authKeyInfo.setAuthKey(value);
            authKeyInfos.add(authKeyInfo);
        });

        authInfo.setAuthKeys(authKeyInfos);
        authInfo.setScope(AuthInfo.ScopeEnum.USER);
        serviceEntity.setAuth(authInfo);
    }

    /**
     * 添加 mcp 服务
     *
     * @param projectId projectId
     * @param body body
     */
    @Override
    @OperationLog
    public McpServerInfo addMcpServer(String projectId, McpServerReq body) {
        log.info("Start to add mcp server for project: {}, server name: {}", projectId, body.getName());

        checkMcpServerParams(projectId, body);
        encryptAuthKeyInfo(body.getAuth() != null ? body.getAuth().getAuthKeys() : null);

        McpServerInfo info = new McpServerInfo();
        info.setProjectId(projectId);

        if (body.getIcon() == null) {
            info.setIcon(defaultIcon);
        } else {
            IconNameCheckUtils.validaIconName(body.getIcon());
            info.setIconName(body.getIcon());
            String icon = ImageBase64Utils.getImageBase64(body.getIcon(), obsService);
            info.setIcon(icon);
        }

        info.setName(body.getName());
        info.setDesc(body.getDesc());
        info.setUrl(body.getUrl());
        info.setAuth(body.getAuth());
        info.setNameEn(body.getNameEn());
        info.setCreatorId(RequestContextUtils.getRequestUserId());
        info.setTools(getMcpServerTools(projectId, body, true));

        if (isOp(projectId)) {
            info.setServerId(body.getNameEn());
            info.setType(CommonConstant.Mcp.TYPE_INNER);
            info.setCreator(CommonConstant.DEFAULT_USERNAME);
            info.setCategory(getCategory(body));
            info.setVisibility(VisibilityEnum.GLOBAL.getValue());

            // 模版服务，需要清空认证信息
            if (body.isIsTemplate()) {
                clearAuthInfo(info.getAuth());
                log.info("Template server, auth info has been cleared. Server ID: {}", info.getServerId());
            }
        } else {
            info.setServerId(UUID.randomUUID().toString());
            info.setType(CommonConstant.Mcp.TYPE_CUSTOM);
            info.setCreator(RequestContextUtils.getRequestUserName());
            info.setCategory(CommonConstant.Mcp.CATEGORY_PUBLIC);
            info.setVisibility(body.getVisibility() == null ? VisibilityEnum.PROJECT.getValue() : body.getVisibility());
        }

        try {
            serverMapper.insert(info);
            log.info("MCP server added successfully. Server ID: {}", info.getServerId());
        } catch (DuplicateKeyException e) {
            log.error("Fail to add mcp server for duplicate name");
            throw new AgentStudioException(StudioError.MCP_NAME_DUPLICATE);
        }

        McpServerInfo result =
            serverMapper.selectByPrimaryKey(projectId, info.getServerId(), RequestContextUtils.getRequestUserId());

        processServerInfoResp(projectId, result);
        log.info("MCP server info processed and returned. Server ID: {}", result.getServerId());

        return result;
    }

    /**
     * MCP 服务工具调测
     *
     * @param projectId project id
     * @param serverId mcp server id
     * @param body 请求体
     * @return 工具调用结果
     */
    @Override
    @OperationLog
    public McpCallToolResp callTool(String projectId, String serverId, McpCallToolReq body) {
        log.info("Start to call tool for project: {}, server ID: {}", projectId, serverId);

        McpServerInfo serverInfo =
            serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());
        if (serverInfo == null) {
            log.error("MCP server not found. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_SERVER_ID_NOT_EXIST, serverId);
        }

        log.info("Server info found, start to recovery auth info. Server ID: {}", serverId);
        recoveryAuthInfo(serverInfo);

        McpCallToolRequest request = McpCallToolRequest.builder()
            .url(serverInfo.getUrl())
            .name(body.getName())
            .params(body.getParams())
            .authInfo(serverInfo.getAuth())
            .build();
        ResponseEntity<McpCallToolResp> response =
            runtimeClient.callMcpServerTool(RequestContextUtils.getRequestAuthToken(), projectId, request);
        McpCallToolResp resp = response.getBody();

        if (!response.getStatusCode().is2xxSuccessful()) {
            String errorMsg = "call mcp tool error with code " + response.getStatusCode();
            log.error("MCP tool call failed. Status code: {}, Error message: {}", response.getStatusCode(), errorMsg);

            resp = new McpCallToolResp().setIsError(true)
                .setContent(List.of(new McpCallToolRespContent().setType("text").setText(errorMsg)));
        }

        return resp;
    }

    /**
     * 配置 mcp 服务
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    @Override
    @OperationLog
    public McpServerConfig configMcpServer(String projectId, String serverId, McpServerConfig body) {
        log.info("Start to configure MCP server. Project ID: {}, Server ID: {}", projectId, serverId);

        checkMcpServerConfig(projectId, serverId, body);
        encryptAuthKeyInfo(body.getAuthKeys());

        McpServerConfig serverConfig = new McpServerConfig();
        serverConfig.setId(UUID.randomUUID().toString());
        serverConfig.setServerId(serverId);
        serverConfig.setProjectId(projectId);
        serverConfig.setCreatorId(RequestContextUtils.getRequestUserId());
        serverConfig.setAuthKeys(body.getAuthKeys());

        try {
            configMapper.insert(serverConfig);
            log.info("MCP server config added successfully. Config ID: {}", serverConfig.getId());
        } catch (DuplicateKeyException e) {
            log.error("Fail to add mcp server config for duplicate reason");
            throw new AgentStudioException(StudioError.MCP_CONFIG_EXIST);
        }

        McpServerConfig config =
            configMapper.selectByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId);
        config.setAuthKeys(null);
        log.info("MCP server config returned successfully. Server ID: {}", serverId);

        return config;
    }

    /**
     * 更新 mcp 服务配置
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    @Override
    @OperationLog
    public McpServerConfig updateMcpServerConfig(String projectId, String serverId, McpServerConfig body) {
        log.info("Start to update MCP server config. Project ID: {}, Server ID: {}", projectId, serverId);

        McpServerConfig config =
                configMapper.selectByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId);

        if (config == null) {
            log.error("MCP server config not found. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_CONFIG_NOT_EXIST);
        }

        checkMcpServerConfig(projectId, serverId, body);

        McpServerConfig update = new McpServerConfig();
        update.setAuthKeys(body.getAuthKeys());
        encryptAuthKeyInfo(update.getAuthKeys());

        log.info("Updating auth keys for server config. Server ID: {}", serverId);
        configMapper.updateByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId,
            JsonUtils.toJson(update.getAuthKeys()));

        if (isAuthKeysChanged(config.getAuthKeys(), update.getAuthKeys())) {
            log.info("Auth keys have changed, updating related resources. Server ID: {}", serverId);
            updateRelationsForServer(projectId, serverId);
        }

        McpServerConfig result =
            configMapper.selectByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId);
        result.setAuthKeys(null);
        log.info("MCP server config updated successfully. Server ID: {}", serverId);

        return result;
    }

    /**
     * 删除 mcp 服务
     *
     * @param projectId projectId
     * @param serverId serverId
     * @return 删除MCP服务响应体
     */
    @Override
    @Transactional
    @OperationLog
    public CommonDeleteRsp deleteMcpServer(String projectId, String serverId) {
        log.info("Start to delete MCP server. Project ID: {}, Server ID: {}", projectId, serverId);

        McpServerInfo info =
            serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());

        if (info == null) {
            log.warn("MCP server not found. Project ID: {}, Server ID: {}", projectId, serverId);
            return null;
        }

        log.info("Checking modify privilege for server. Server ID: {}", serverId);
        checkModifyPrivilege(info, projectId, RequestContextUtils.getRequestUserId());

        log.info("Updating related mappings for server. Server ID: {}", serverId);
        mappingMapper.updateValidByResourceIdAndVersionId(serverId, null);

        log.info("Deleting server from database. Server ID: {}", serverId);
        serverMapper.deleteByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());

        log.info("MCP server deleted successfully. Server ID: {}", serverId);
        return new CommonDeleteRsp().setId(serverId);
    }

    /**
     * 删除服务配置
     *
     * @param projectId projectId
     * @param serverId serverId
     * @return 删除MCP服务配置响应体
     */
    @Override
    @OperationLog
    public CommonDeleteRsp deleteMcpServerConfig(String projectId, String serverId) {
        log.info("Start to delete MCP server config. Project ID: {}, Server ID: {}", projectId, serverId);

        McpServerConfig config =
            configMapper.selectByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId);

        if (config == null) {
            log.warn("MCP server config not found. Project ID: {}, Server ID: {}", projectId, serverId);
            return null;
        }

        log.info("Deleting server config from database. Server ID: {}", serverId);
        configMapper.deleteByUniqueIds(projectId, RequestContextUtils.getRequestUserId(), serverId);

        log.info("MCP server config deleted successfully. Server ID: {}", serverId);
        return new CommonDeleteRsp().setId(serverId);
    }

    /**
     * 获取 Mcp 服务详细信息
     *
     * @param projectId projectId
     * @param serverId serverId
     */
    @Override
    public McpServerInfo getMcpServer(String projectId, String serverId) {
        log.info("Start to get MCP server info. Project ID: {}, Server ID: {}", projectId, serverId);

        McpServerInfo serverInfo =
            serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());

        if (serverInfo == null) {
            log.error("MCP server not found. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_NOT_EXIST);
        }

        // 根据语言处理返回结果
        Locale locale = LanguageUtils.getLanguageLocale();

        // 如果creator是“官方预置”，进行替换
        if (CommonConstant.DEFAULT_USERNAME.equals(serverInfo.getCreator())) {
            serverInfo.setCreator(messageSource.getMessage("mcpserver.creator.official", null, locale));
        }

        log.info("Processing server info response for server ID: {}", serverId);
        processServerInfoResp(projectId, serverInfo);

        log.info("MCP server info retrieved successfully. Server ID: {}", serverId);
        return serverInfo;
    }

    /**
     * 返回 MCP 服务工具列表
     *
     * @param projectId projectId
     * @param offset offset
     * @param limit limit
     * @param body body
     */
    @Override
    public McpServerTools listMcpServerTools(String projectId, Integer offset, Integer limit, McpServerReq body) {
        log.info("Start to list MCP server tools. Project ID: {}", projectId);

        if (StringUtils.isBlank(body.getUrl())) {
            log.error("MCP server URL is empty. Project ID: {}", projectId);
            throw new AgentStudioException(StudioError.MCP_SERVER_URL_EMPTY);
        }

        log.info("Fetching server tools for project: {}", projectId);
        List<McpServerTool> tools = getMcpServerTools(projectId, body, false).getTools();

        if (StringUtils.isNotBlank(body.getName())) {
            log.info("Filtering tools by name: {}", body.getName());
            tools = tools.stream()
                .filter(
                    item -> item.getName().toLowerCase(Locale.ROOT).contains(body.getName().toLowerCase(Locale.ROOT)))
                .toList();
        }

        int startIndex = offset;
        int endIndex = Math.min(startIndex + limit, tools.size());

        McpServerTools serverTools = new McpServerTools();
        serverTools.setCount(tools.size());
        serverTools.setTools(startIndex > endIndex ? new ArrayList<>() : tools.subList(startIndex, endIndex));

        log.info("MCP server tools listed successfully.");
        return serverTools;
    }


    /**
     * 查询 mcp 服务列表
     *
     * @param projectId projectId
     * @param listMcpServersQo 过滤条件
     * @return mcp 服务列表
     */
    @Override
    public McpServerInfoList listMcpServers(String projectId, ListMcpServersQo listMcpServersQo) {
        log.info("Start to list MCP servers. Project ID: {}", projectId);

        log.info("Setting pagination with offset: {}, limit: {}", listMcpServersQo.getOffset(), listMcpServersQo.getLimit());
        PageHelper.offsetPage(listMcpServersQo.getOffset(), listMcpServersQo.getLimit());

        if (StringUtils.isBlank(listMcpServersQo.getType())) {
            log.info("Server type is not specified. Setting default type based on project.");
            listMcpServersQo.setType(isOp(projectId) ? CommonConstant.Mcp.TYPE_INNER : CommonConstant.Mcp.TYPE_CUSTOM);
        } else if (listMcpServersQo.getType().equals(ToolType.ALL.type)) {
            log.info("Server type is ALL, resetting to null to fetch all types.");
            listMcpServersQo.setType(null);
        }

        log.info("Querying server info list from database.");
        List<McpServerInfo> serverInfoList =
            serverMapper.selectBySearchCriteria(projectId, RequestContextUtils.getRequestUserId(), listMcpServersQo);
        serverInfoList.forEach(info -> processServerInfoResp(projectId, info));

        // 根据语言处理返回结果
        Locale locale = LanguageUtils.getLanguageLocale();

        log.info("Replacing creator name for default user if exists.");
        serverInfoList.forEach(serverInfo -> {
            if (CommonConstant.DEFAULT_USERNAME.equals(serverInfo.getCreator())) {
                serverInfo.setCreator(messageSource.getMessage("mcpserver.creator.official", null, locale));
            }
        });

        PageInfo<McpServerInfo> pageInfo = new PageInfo<>(serverInfoList);
        McpServerInfoList list = new McpServerInfoList();
        list.setServers(serverInfoList);
        list.setCount(pageInfo.getTotal());

        log.info("MCP server list retrieved successfully. Total: {}, Returned: {}", pageInfo.getTotal(), serverInfoList.size());
        return list;
    }


    /**
     * 更新 mcp 服务
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    @Override
    @Transactional
    public McpServerInfo updateMcpServer(String projectId, String serverId, McpServerReq body) {
        log.info("Start to update MCP server. Project ID: {}, Server ID: {}", projectId, serverId);

        McpServerInfo info =
            serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());
        if (info == null) {
            log.error("MCP server not found. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_NOT_EXIST);
        }

        if (!Objects.equals(info.getUrl(), body.getUrl())) {
            log.error("Attempt to update MCP server URL which is not allowed. Server ID: {}", serverId);
            throw new AgentStudioException(StudioError.UPDATE_MCP_URL_NOT_ALLOW);
        }

        log.info("Checking modify privilege for server. Server ID: {}", serverId);
        checkModifyPrivilege(info, projectId, RequestContextUtils.getRequestUserId());

        log.info("Validating server parameters.");
        checkMcpServerParams(projectId, body);

        log.info("Encrypting authentication keys if provided.");
        encryptAuthKeyInfo(body.getAuth() != null ? body.getAuth().getAuthKeys() : null);

        McpServerInfo updateInfo = new McpServerInfo();
        updateInfo.setName(body.getName());
        updateInfo.setDesc(body.getDesc());
        updateInfo.setUrl(body.getUrl());
        updateInfo.setAuth(body.getAuth());
        updateInfo.setNameEn(body.getNameEn());

        if (body.getIcon() == null) {
            updateInfo.setIcon(info.getIcon());
        } else {
            log.info("Validating and updating server icon.");
            IconNameCheckUtils.validaIconName(body.getIcon());
            updateInfo.setIconName(body.getIcon());
            String icon = ImageBase64Utils.getImageBase64(body.getIcon(), obsService);
            updateInfo.setIcon(icon);
        }
        updateInfo.setAuth(body.getAuth());
        updateInfo.setNameEn(body.getNameEn());
        updateInfo.setUrl(body.getUrl());
        updateInfo.setTools(getMcpServerTools(projectId, body, true));

        if (isOp(projectId)) {
            updateInfo.setCategory(getCategory(body));
            // 模版服务，需要清空认证信息
            if (body.isIsTemplate()) {
                log.info("Clearing authentication info for template server. Server ID: {}", serverId);
                clearAuthInfo(updateInfo.getAuth());
            }
        } else {
            updateInfo.setCategory(CommonConstant.Mcp.CATEGORY_PUBLIC);
        }

        log.info("Updating server info in database. Server ID: {}", serverId);
        try {
            serverMapper.updateById(projectId, serverId, RequestContextUtils.getRequestUserId(), updateInfo);

            List<MappingEntity> mappingEntities = mappingMapper.selectByResourceId(serverId);
            if (mappingEntities != null) {
                for (MappingEntity mappingEntity : mappingEntities) {
                    mappingEntity.setResourceName(body.getName());
                    mappingEntity.setResourceDesc(body.getDesc());
                    log.info("Updating mapping entity for server ID: {}", serverId);
                    mappingMapper.updateByPrimaryKeySelective(mappingEntity);
                }
            }
        } catch (DuplicateKeyException e) {
            log.error("Failed to update server due to duplicate name. Server ID: {}", serverId);
            throw new AgentStudioException(StudioError.MCP_NAME_DUPLICATE);
        }

        if (isAuthInfoChanged(info.getAuth(), updateInfo.getAuth())) {
            log.info("Authentication info has changed, updating related resources. Server ID: {}", serverId);
            updateRelationsForServer(projectId, serverId);
        }

        log.info("MCP server updated successfully. Server ID: {}", serverId);
        return new McpServerInfo().setServerId(serverId);
    }


    private void checkMcpServerParams(String projectId, McpServerReq req) {
        log.info("Start to validate MCP server parameters. Project ID: {}", projectId);

        if (StringUtils.isBlank(req.getName())) {
            log.error("MCP server name is empty. Project ID: {}", projectId);
            throw new AgentStudioException(StudioError.MCP_NAME_EMPTY);
        }

        if (StringUtils.isBlank(req.getNameEn())) {
            log.error("MCP server English name is empty. Project ID: {}", projectId);
            throw new AgentStudioException(StudioError.MCP_ENGLISH_EMPTY);
        }

        if (StringUtils.isBlank(req.getDesc())) {
            log.error("MCP server description is empty. Project ID: {}", projectId);
            throw new AgentStudioException(StudioError.MCP_DESCRIPTION_EMPTY);
        }

        if (StringUtils.isNotEmpty(req.getVisibility())
            && Arrays.stream(VisibilityEnum.values()).noneMatch(item -> item.getValue().equals(req.getVisibility()))) {
            log.error("Visibility value:{} is invalid.", req.getVisibility());
            throw new AgentStudioException(StudioError.MCP_VISIBILITY_INVALID);
        }

        if (StringUtils.isBlank(req.getUrl())) {
            log.error("MCP server URL is empty. Project ID: {}", projectId);
            throw new AgentStudioException(StudioError.MCP_SERVER_URL_EMPTY);
        }

        log.info("Validating server URL format.");
        urlCheckUtils.checkUrl(projectId, req.getUrl());

        log.info("MCP server parameters validated successfully. Project ID: {}", projectId);
    }

    private void encryptAuthKeyInfo(List<AuthKeyInfo> authKeyInfos) {
        if (authKeyInfos != null) {
            authKeyInfos.forEach(item -> {
                if (StringUtils.isNotBlank(item.getAuthKey())) {
                    item.setAuthKey(encryptionAdapter.encrypt(item.getAuthKey(), RequestContextUtils.getRequestUserDomainId()));
                }
            });
        }
    }

    private void processServerInfoResp(String projectId, McpServerInfo serverInfo) {
        clearAuthInfo(serverInfo.getAuth());
        if (!isOp(projectId) && CommonConstant.Mcp.TYPE_INNER.equals(serverInfo.getType())) {
            serverInfo.setUrl(null);
        }
        serverInfo.setConfigs(null);
    }

    private void clearAuthInfo(AuthInfo authInfo) {
        if (authInfo == null || authInfo.getAuthKeys() == null) {
            return;
        }
        authInfo.getAuthKeys().forEach(item -> {
            item.setAuthKey("");
        });
    }

    private void checkModifyPrivilege(McpServerInfo info, String projectId, String userId) {
        if (!opProjectId.equals(projectId) && !info.getCreatorId().equals(userId)) {
            throw new AgentStudioException(StudioError.MCP_PERMISSION_DENY);
        }
    }

    private boolean isOp(String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return false;
        }
        return projectId.equals(opProjectId);
    }

    private String getCategory(McpServerReq req) {
        log.info("Determining server category based on auth info.");

        AuthInfo authInfo = req.getAuth();
        String category = CommonConstant.Mcp.CATEGORY_PUBLIC;

        if (authInfo != null && !CollectionUtils.isEmpty(authInfo.getAuthKeys())) {
            if (req.isIsTemplate()) {
                log.info("Server is marked as template, setting category to TEMPLATE.");
                return CommonConstant.Mcp.CATEGORY_TEMPLATE;
            }

            for (AuthKeyInfo keyInfo : authInfo.getAuthKeys()) {
                if (StringUtils.isBlank(keyInfo.getAuthKey())) {
                    log.info("Found empty auth key, setting category to TEMPLATE.");
                    category = CommonConstant.Mcp.CATEGORY_TEMPLATE;
                    break;
                }
            }
        }

        log.info("Server category determined as: {}", category);
        return category;
    }

    private void checkMcpServerConfig(String projectId, String serverId, McpServerConfig serverConfig) {
        log.info("Start to validate MCP server config. Project ID: {}, Server ID: {}", projectId, serverId);

        if (serverConfig.getAuthKeys() == null) {
            log.error("MCP server config has no auth keys. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_CONFIG_ERROR);
        }

        log.info("Checking if server exists. Server ID: {}", serverId);
        McpServerInfo info =
                serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());

        if (info == null) {
            log.error("MCP server not found. Project ID: {}, Server ID: {}", projectId, serverId);
            throw new AgentStudioException(StudioError.MCP_SERVER_ID_NOT_EXIST, serverId);
        }

        if (!info.getType().equals(CommonConstant.Mcp.TYPE_INNER)) {
            log.error("Cannot configure non-inner server. Server ID: {}", serverId);
            throw new AgentStudioException(StudioError.MCP_CANNOT_CONFIG);
        }

        if (info.getCategory().equals(CommonConstant.Mcp.CATEGORY_PUBLIC)) {
            log.error("Cannot configure public server. Server ID: {}", serverId);
            throw new AgentStudioException(StudioError.MCP_CANNOT_CONFIG_PUBLIC);
        }

        log.info("Validating auth keys in config. Server ID: {}", serverId);
        List<String> missingKeys = new ArrayList<>();
        Set<String> configKeys = new HashSet<>();

        for (AuthKeyInfo keyInfo : serverConfig.getAuthKeys()) {
            if (StringUtils.isBlank(keyInfo.getAuthKey())) {
                log.error("Auth key is empty for target: {} in config. Server ID: {}", keyInfo.getTargetName(), serverId);
                throw new AgentStudioException(StudioError.MCP_CONFIG_ERROR_EMPTY, keyInfo.getTargetName());
            }
            configKeys.add(keyInfo.getTargetName());
        }

        for (AuthKeyInfo keyInfo : info.getAuth().getAuthKeys()) {
            // 存在没有值key，并且在serverConfig中没有配置
            if (!configKeys.contains(keyInfo.getTargetName()) && StringUtils.isBlank(keyInfo.getAuthKey())) {
                missingKeys.add(keyInfo.getTargetName());
            }
        }

        if (!missingKeys.isEmpty()) {
            log.error("Missing auth keys in config: {} for server ID: {}", missingKeys, serverId);
            throw new AgentStudioException(StudioError.MCP_CONFIG_MISS_KEY, missingKeys);
        }

        log.info("MCP server config validated successfully. Server ID: {}", serverId);
    }

    private boolean isAuthInfoChanged(AuthInfo oldInfo, AuthInfo newInfo) {
        if (oldInfo == null && newInfo == null) {
            return false;
        } else if (oldInfo == null || newInfo == null) {
            return true;
        }

        if (!Objects.equals(oldInfo.getDomain(), newInfo.getDomain())
            || !Objects.equals(oldInfo.getScope(), newInfo.getScope())) {
            return true;
        }

        return isAuthKeysChanged(oldInfo.getAuthKeys(), newInfo.getAuthKeys());
    }

    private boolean isAuthKeysChanged(List<AuthKeyInfo> oldKeys, List<AuthKeyInfo> newKeys) {
        if (oldKeys == null && newKeys == null) {
            return false;
        } else if (oldKeys == null || newKeys == null || oldKeys.size() != newKeys.size()) {
            return true;
        }

        Map<String, AuthKeyInfo> oldKeyMap = new HashMap<>();
        oldKeys.forEach(item -> oldKeyMap.put(item.getTargetName(), item));
        for (AuthKeyInfo newKeyInfo : newKeys) {
            AuthKeyInfo oldKeyInfo = oldKeyMap.get(newKeyInfo.getTargetName());
            if (oldKeyInfo == null) {
                return true;
            }
            String oldValue =
                StringUtils.isBlank(oldKeyInfo.getAuthKey()) ? "" : encryptionAdapter.decrypt(oldKeyInfo.getAuthKey());
            String newValue =
                StringUtils.isBlank(newKeyInfo.getAuthKey()) ? "" : encryptionAdapter.decrypt(newKeyInfo.getAuthKey());
            if (!Objects.equals(oldValue, newValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新 mcp 服务关联的应用 ir
     *
     * @param serverId mcp 服务 id
     */
    private void updateRelationsForServer(String projectId, String serverId) {
        log.info("Auth info changed, update related app ir for server {}", serverId);
        List<MappingEntity> entities = mappingMapper.selectByResourceId(serverId);
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        McpServerInfo info =
            serverMapper.selectByPrimaryKey(projectId, serverId, RequestContextUtils.getRequestUserId());
        if (info == null) {
            log.error("mcp server does not exist, id = {}", serverId);
            return;
        }
        for (MappingEntity entity : entities) {
            if (entity.getAppType().equals(CommonConstant.WORKFLOW_TYPE)) {
                WorkflowEntity workflowEntity = workflowCommonService.getWorkflowByWorkspaceAndOpProject(projectId,
                    ThreadLocalUtils.getWorkspaceId(), entity.getAppId());
                if (workflowEntity == null) {
                    log.error("workflow does not exist, projectId = {}, workflow = {}", projectId, entity.getAppId());
                    continue;
                }
                if (Strings.CI.equals(workflowEntity.getVisibility(), VisibilityEnum.USER.getValue())
                    && !Strings.CS.equals(workflowEntity.getCreatorId(), RequestContextUtils.getRequestUserId())) {
                    log.error("workflow update not allowed, creatorId = {}, userId = {}", workflowEntity.getCreatorId(),
                        RequestContextUtils.getRequestUserId());
                    continue;
                }
                updateWorkflowIr(projectId, workflowEntity, info);
            } else if (entity.getAppType().equals(CommonConstant.AGENT_TYPE)) {
                Agent agent = agentMapper.selectByPrimaryKey(projectId, entity.getAppId());
                if (agent == null) {
                    log.error("agent does not exist, projectId = {}, agentId = {}", projectId, entity.getAppId());
                    continue;
                }
                updateAgentIr(agent, info);
            }
        }
    }

    private void updateWorkflowIr(String projectId, WorkflowEntity workflowEntity, McpServerInfo info) {
        log.info("Start to update workflow IR. Project ID: {}, Workflow ID: {}", projectId, workflowEntity.getId());

        log.info("Downloading IR file from OBS");
        String irInfo = obsService.downloadObsFile(workflowEntity.getIrPath());

        log.info("Parsing JSON content of IR file.");
        JSONObject jsonObject = JSON.parseObject(irInfo);

        if (jsonObject.get("components") == null) {
            log.warn("No components found in IR file. Nothing to update.");
            return;
        }

        boolean isUpdate = false;
        JSONArray components = jsonObject.getJSONArray("components");

        log.info("Iterating through components to find MCP type.");
        for (Object obj : components) {
            JSONObject component = (JSONObject) obj;
            if (NodeType.MCP.getIrType().equals(component.get("type"))) {
                JSONObject configs = component.getJSONObject("configs");
                String serverId = configs.getString("id");

                if (StringUtils.isBlank(serverId) || !serverId.equals(info.getServerId())) {
                    continue;
                }

                log.info("Found matching MCP component. Updating auth info.");
                recoveryAuthInfo(info);
                configs.put("auth", WorkflowUtils.parseAuthInfo(info.getAuth()));
                isUpdate = true;
            }
        }

        if (isUpdate) {
            String workflowId = workflowEntity.getId();
            log.info("Updating IR file content and uploading to OBS. Workflow ID: {}", workflowId);

            String irPath = obsService.uploadObsFile(workflowId, workflowId, CommonConstant.WORKFLOW,
                JSON.toJSONString(jsonObject, JSONWriter.Feature.WriteMapNullValue), CommonConstant.Workflow.IR);
            workflowEntity.setIrPath(irPath);
            log.info("Updating workflow entity in database. Workflow ID: {}", workflowId);
            workflowMapper.updateWorkflowEntity(projectId, workflowId, workflowEntity);
        } else {
            log.info("No MCP component found to update. Workflow IR remains unchanged.");
        }
    }

    private void updateAgentIr(Agent agent, McpServerInfo serverInfo) {
        log.info("Start to update agent IR.");

        log.info("Downloading IR file from OBS. ");
        String irInfo = obsService.downloadObsFile(agent.getIrPath());

        log.info("Parsing JSON content of IR file.");
        JSONObject jsonObject = JSON.parseObject(irInfo);
        JSONArray mcpServers = (JSONArray) ((JSONObject) jsonObject.get("configs")).get("mcps");
        if (mcpServers == null || mcpServers.size() == 0) {
            log.warn("No MCP servers found in IR file. Nothing to update.");
            return;
        }

        boolean isUpdate = false;
        log.info("Iterating through MCP servers in IR file.");
        for (Object obj : mcpServers) {
            JSONObject server = (JSONObject) obj;
            String serverId = server.getString("id");

            if (StringUtils.isBlank(serverId) || !serverId.equals(serverInfo.getServerId())) {
                continue;
            }

            log.info("Found matching MCP server. Updating auth info. Server ID: {}", serverId);
            recoveryAuthInfo(serverInfo);
            server.put("auth", WorkflowUtils.parseAuthInfo(serverInfo.getAuth()));
            isUpdate = true;
        }

        if (isUpdate) {
            log.info("Updating IR file content and uploading to OBS. Agent ID: {}", agent.getAgentId());
            obsService.uploadObsFile(agent.getAgentId(), agent.getAgentId(), CommonConstant.AGENT,
                JSON.toJSONString(jsonObject, JSONWriter.Feature.WriteMapNullValue), CommonConstant.Workflow.IR);
        }
    }

    private McpServerTools getMcpServerTools(String projectId, McpServerReq req, boolean isEncrypted) {
        log.info("Start to get MCP server tools. Project ID: {}", projectId);

        McpServerReq request = new McpServerReq();
        request.setUrl(req.getUrl());

        if (req.getAuth() != null) {
            log.info("Cloning and setting authentication info.");
            request.setAuth(SerializationUtils.clone(req.getAuth()));
        }

        if (!isEncrypted && request.getAuth() != null) {
            log.info("Encrypting auth keys before sending request.");
            encryptAuthKeyInfo(request.getAuth().getAuthKeys());
        }

        log.info("Calling remote service to query MCP server tools.");
        ResponseEntity<McpServerTools> response =
            runtimeClient.queryMcpServerTools(RequestContextUtils.getRequestAuthToken(), projectId, request);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to get MCP server tools. Status code: {}", response.getStatusCode());
            throw new AgentStudioException(StudioError.MCP_TOOL_ERROR, response.getStatusCode());
        }

        McpServerTools serverTools = response.getBody();

        if (Objects.isNull(serverTools)) {
            log.warn("Server tools response is null. Creating empty response.");
            serverTools = new McpServerTools().setCount(0).setTools(new ArrayList<>());
        }

        log.info("MCP server tools retrieved successfully. Count: {}", serverTools.getCount());
        return serverTools;
    }
}
