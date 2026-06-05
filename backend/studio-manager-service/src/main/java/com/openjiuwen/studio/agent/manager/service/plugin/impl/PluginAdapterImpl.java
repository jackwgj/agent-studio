/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.plugin.impl;

import static com.openjiuwen.studio.agent.common.enums.NodeType.PLUGIN;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.dto.ToolReference;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.dto.plugin.ToolInfo;
import com.openjiuwen.studio.agent.manager.dto.plugin.ToolRequestInfo;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowExportEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.enums.ToolType;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class PluginAdapterImpl implements IPlugin {
    @Autowired
    private ShareInnerService shareInnerService;

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private IPluginBase pluginBase;

    @Autowired
    private MgObsService obsService;

    @Autowired
    private ReleaseVersionMapper releaseVersionMapper;

    @Autowired
    private EncryptionAdapter encryptionAdapter;

    @Value("${asset.free.trial-quota-limit:10}")
    private int pluginMaxFreeTrialTimes;

    private static final String PLUGIN_FREE_TRIAL_USAGE_QUOTA_KEY_FORMAT
            = "agent.manager.asset.plugin.free.trial.usage.quota.asset_%s.month_%d.domain_%s";

    @Autowired
    private RedisClient redisClient;

    @Override
    public List<ToolReference> getToolReferences(String projectId, String workspaceId, List<String> pluginIds) {
        log.info("start to get tool References!");
        Map<String, List<String>> tools = convertToolIds2PluginToolMap(pluginIds);
        List<String> pluginIsSharedIds = filterIdIsShared(tools.keySet().stream().toList());
        List<String> pluginCurUser = filterPluginIds(tools.keySet().stream().toList(), pluginIsSharedIds);
        List<PluginEntity> input = new ArrayList<>();
        List<PluginEntity> pluginEntities = pluginMapper.selectByPrimaryIdAndToolIdsWithSearchCriteria(projectId,
            workspaceId, pluginCurUser, null);
        List<PluginEntity> pluginEntitiesIsShared = pluginMapper.selectByPrimaryIdAndToolIdsWithSearchCriteria(
            projectId, null, pluginIsSharedIds, null);
        if (CollectionUtils.isNotEmpty(pluginEntities)) {
            input.addAll(pluginEntities);
        }
        if (CollectionUtils.isNotEmpty(pluginEntitiesIsShared)) {
            input.addAll(pluginEntitiesIsShared);
        }
        List<ToolReference> result = new ArrayList<>();
        for (PluginEntity plugin : input) {
            if (isNewTool(plugin)) {
                List<ToolReference> toolReferences = buildTools(plugin, tools.get(plugin.getPluginId()), workspaceId);
                result.addAll(toolReferences);
            } else {
                PluginEntity pluginEntity = pluginMapper.selectByPrimaryKeyAndWorkspaceAndCredential(
                    plugin.getPluginId(), null, workspaceId);
                ToolReference toolReference = new ToolReference();
                toolReference.setToolId(plugin.getPluginId() + "#0");
                toolReference.setPluginDisplayName(plugin.getPluginDisplayName());
                toolReference.setPluginChineseName(plugin.getPluginChineseName());
                toolReference.setToolDisplayName(plugin.getPluginDisplayName());
                toolReference.setToolChineseName(plugin.getPluginChineseName());
                if (ToolType.INNER.type.equals(plugin.getType())) {
                    toolReference.setToolChineseName(plugin.getPluginDisplayName());
                }
                toolReference.setToolIcon(plugin.getIcon());
                toolReference.setDesc(plugin.getPluginDesc());
                toolReference.setCredentialStatus(pluginEntity.getCredentialStatus());
                toolReference.setAuthInfo(anonymizeAuthInfo(pluginEntity.getAuthInfo()));
                setupFreeTrialQuota(plugin, toolReference, pluginMaxFreeTrialTimes);
                toolReference.setIsFree(plugin.getIsFree());
                if (StringUtils.isNotEmpty(plugin.getLastVersionId())) {
                    ReleaseVersion releaseVersion = releaseVersionMapper.selectByAppIdAndVersionId(plugin.getPluginId(),
                        plugin.getLastVersionId());
                    if (ObjectUtils.isNotEmpty(releaseVersion)) {
                        toolReference.setLastVersionId(plugin.getLastVersionId());
                        toolReference.setLastVersionName(releaseVersion.getVersionName());
                    }
                }
                result.add(toolReference);
            }
        }
        return result;
    }

    private String getPluginFreeTrialUsageQuotaKey(String domainId, String pluginId) {
        return String.format(Locale.ROOT, PLUGIN_FREE_TRIAL_USAGE_QUOTA_KEY_FORMAT, pluginId,
                LocalDate.now().getMonthValue(), domainId);
    }

    private List<String> filterIdIsShared(List<String> tools) {
        List<ShareResourceEntity> shareScopes = shareInnerService.getOriginSharedInfoByResourceId(tools);
        List<String> result = shareScopes.stream().map(ShareResourceEntity::getResourceId).toList();
        log.info(">>>filterIdIsShared id {}", result);
        return result;
    }

    private List<String> filterPluginIds(List<String> originIds, List<String> deleteIds) {
        // 创建一个新的列表用于存储结果
        List<String> result = new ArrayList<>();

        // 过滤 pluginIds
        for (String id : originIds) {
            if (!deleteIds.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    private List<ToolReference> buildTools(PluginEntity plugin, List<String> targetTools, String workspaceId) {
        List<ToolReference> result = new ArrayList<>();
        if (ObjectUtils.isEmpty(plugin.getRequestInfo())) {
            return result;
        }

        PluginEntity pluginEntity = pluginMapper.selectByPrimaryKeyAndWorkspaceAndCredential(plugin.getPluginId(), null,
            workspaceId);
        AuthInfo authInfo = anonymizeAuthInfo(pluginEntity.getAuthInfo());
        JSONObject jsonObject = JSON.parseObject(plugin.getRequestInfo());
        List<ToolInfo> toolsInfos = jsonObject.getList("tool_info", ToolInfo.class);
        for (ToolInfo toolInfo : toolsInfos) {
            if (targetTools.contains(toolInfo.getToolId())) {
                ToolReference toolReference = new ToolReference();
                toolReference.setToolId(plugin.getPluginId() + "#" + toolInfo.getToolId());
                toolReference.setPluginDisplayName(plugin.getPluginDisplayName());
                toolReference.setPluginChineseName(plugin.getPluginChineseName());
                toolReference.setToolDisplayName(toolInfo.getToolDisplayName());
                toolReference.setToolChineseName(toolInfo.getToolChineseName());
                toolReference.setToolIcon(plugin.getIcon());
                toolReference.setDesc(toolInfo.getToolDesc());
                toolReference.setCredentialStatus(pluginEntity.getCredentialStatus());
                toolReference.setAuthInfo(authInfo);
                toolReference.setMetadata(plugin.getMetadata());
                toolReference.setIsFree(plugin.getIsFree());
                setupFreeTrialQuota(plugin, toolReference, pluginMaxFreeTrialTimes);
                if (StringUtils.isNotEmpty(plugin.getLastVersionId())) {
                    ReleaseVersion releaseVersion = releaseVersionMapper.selectByAppIdAndVersionId(plugin.getPluginId(),
                        plugin.getLastVersionId());
                    if (ObjectUtils.isNotEmpty(releaseVersion)) {
                        toolReference.setLastVersionId(plugin.getLastVersionId());
                        toolReference.setLastVersionName(releaseVersion.getVersionName());
                    }
                }
                result.add(toolReference);
            }
        }
        return result;
    }

    /**
     * 设置插件的免费试用额度（仅适用于内置免费插件）
     *
     * @param plugin               插件信息（必须包含 type 和 isFree 字段）
     * @param toolReference        工具引用对象，用于设置使用次数和上限
     * @param pluginMaxFreeTrialTimes 最大免费试用次数
     */
    public void setupFreeTrialQuota(PluginEntity plugin, ToolReference toolReference,
                                           int pluginMaxFreeTrialTimes) {
        // 内置免费额度插件
        if (ToolType.INNER.type.equals(plugin.getType()) && plugin.getIsFree() != null && plugin.getIsFree() == 1) {
            toolReference.setLimit(pluginMaxFreeTrialTimes);
            try {
                String domainId = RequestContextUtils.getRequestUserDomainId();
                String pluginFreeTrialUsageRecords =
                        redisClient.get(getPluginFreeTrialUsageQuotaKey(domainId, plugin.getPluginId()));

                if (StringUtils.isBlank(pluginFreeTrialUsageRecords)) {
                    log.info("None plugin free trial quota record stored, return zero as usage quota. " +
                            "Domain: {}, plugin: {}.", domainId, plugin.getPluginId());
                    toolReference.setUsage(0);
                }
                toolReference.setUsage(NumberUtils.toInt(pluginFreeTrialUsageRecords));
            } catch (Exception e) {
                throw new AgentStudioException(StudioError.PLUGIN_FREE_TRIAL_USAGE_QUOTA_ERROR);
            }
        }
    }


    @Override
    public boolean isNewTool(PluginEntity pluginEntity) {
        if (pluginEntity == null) {
            return false; // 或抛出异常，视业务而定
        }
        return pluginEntity.getRequestInfo().contains("basic_info");
    }

    /**
     * @param pluginEntity 插件对象
     * @return 新插件的resourceid 使用 插件id与工具id `#` 相连
     */
    @Override
    public List<String> buildResourceId(PluginEntity pluginEntity) {
        JSONObject jsonObject = JSON.parseObject(pluginEntity.getRequestInfo());
        List<ToolInfo> toolsInfo = jsonObject.getList("tool_info", ToolInfo.class);
        return toolsInfo.stream().map(toolInfo -> pluginEntity.getPluginId() + "#" + toolInfo.getToolId()).toList();
    }

    /**
     * ir转换专用
     *
     * @param id id
     * @param projectId projectId
     * @param opProjectId opProjectId
     * @param versionId versionId
     * @return ir中的插件
     */
    @Override
    public ToolEntity getToolEntity(String id, String projectId, String opProjectId, String versionId) {
        ToolEntity toolEntity;
        if (StringUtils.isNotEmpty(versionId)) {
            toolEntity = pluginBase.getToolEntityByVersion(id, versionId);
            toolEntity.setVersionId(versionId);
        } else {
            String[] ids = id.split("#");
            String pluginId = ids[0]; // 获取 pluginId
            // 确保 toolId 存在
            String toolId = ids.length > 1 ? ids[1] : "0"; // 如果存在 toolId, 否则为 null
            toolEntity = pluginBase.buildToolByPlugin(projectId, null, pluginId, toolId);
        }

        return toolEntity;
    }

    @Override
    public void validTools(String projectId, String workspaceId, List<String> toolIds) {
        Map<String, List<String>> toolMap = convertToolIds2PluginToolMap(toolIds);
        List<PluginEntity> toolEntities = pluginMapper.selectByIds(toolMap.keySet().stream().toList());
        if (CollectionUtils.isEmpty(toolEntities)) {
            throw new AgentStudioException(StudioError.TOOLS_NOT_EXIST_OR_NO_PERMISSION, String.join(",", toolIds));
        }

        // 增加共享判断，如果插件被共享到当前空间,则可以使用
        List<String> canUseIds = shareInnerService.getShareScopeByWorkspaceAndId(toolMap.keySet().stream().toList(),
            workspaceId).stream().map(ShareScopeEntity::getResourceId).toList();
        List<String> cannotUseIds = toolEntities.stream()
            .filter(tool -> "custom".equals(tool.getType()) && (!Strings.CS.equals(projectId, tool.getProjectId())
                || !Strings.CS.equals(workspaceId, tool.getWorkspaceId())))
            .map(PluginEntity::getPluginId)
            .toList();

        if (!CollectionUtils.isEmpty(canUseIds)) {
            log.info(">>>validTools {} was been shared.", canUseIds);
            return;
        }

        if (!CollectionUtils.isEmpty(cannotUseIds)) {
            throw new AgentStudioException(StudioError.TOOLS_NOT_EXIST_OR_NO_PERMISSION,
                String.join(",", cannotUseIds));
        }
    }

    @Override
    public void updateOldTool(String projectId, String versionId, String toolId) {
        // 更新最新版本号
        PluginEntity oldTool = pluginMapper.selectByPrimaryKey(toolId, projectId);
        String oldVersionId = oldTool.getLastVersionId();
        if (StringUtils.isEmpty(oldVersionId) || Long.parseLong(versionId) > Long.parseLong(oldVersionId)) {
            oldTool.setLastVersionId(versionId);
            pluginMapper.updateByPrimaryKeySelective(oldTool);
        }
    }

    @Override
    public void insertByTool(ToolEntity tool) {
        handleAdapterToolId(tool);
        pluginMapper.insert(pluginBase.transformTool2Plugin(tool));
    }

    @Override
    public void updateByOldTool(ToolEntity tool) {
        handleAdapterToolId(tool);
        pluginMapper.updateByPrimaryKeySelective(pluginBase.transformTool2Plugin(tool));
    }

    public void handleAdapterToolId(ToolEntity tool) {
        String[] ids = tool.getToolId().split("#");
        String pluginId = ids[0]; // 获取 pluginId
        tool.setToolId(pluginId);
    }

    @Override
    public ReleaseVersion releaseToolVersionHandler(String toolId, String versionId, String versionName,
        String versionNote) {
        PluginEntity toolEntity = pluginMapper.selectByPrimaryKey(toolId, null);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        toolId = toolEntity.getPluginId();
        releaseVersion.setVersionId(
            StringUtils.isBlank(versionId) ? String.valueOf(System.currentTimeMillis()) : versionId);

        // 发布到OBS
        toolEntity.setLastVersionId(null);
        String releaseDslPath = obsService.uploadObsFile(toolId,
            toolId + Constants.UNDERLINE_STR + releaseVersion.getVersionId(), CommonConstant.TOOL,
            JsonUtils.toJson(toolEntity), CommonConstant.DSL_STR);
        releaseVersion.setDslPath(releaseDslPath);

        // 新版本insert t_release_version
        toolEntity.setPublished(1);
        pluginMapper.updateByPrimaryKeySelective(toolEntity);
        releaseVersion.setId(UUID.randomUUID().toString());
        releaseVersion.setReleasedOn(new Date());

        releaseVersion.setVersionName(versionName);
        releaseVersion.setVersionNote(versionNote);

        releaseVersion.setAppType(CommonConstant.TOOL_TYPE);
        releaseVersion.setStatus(CommonConstant.NORMAL);

        releaseVersion.setAppId(toolEntity.getPluginId());

        releaseVersion.setCreator(RequestContextUtils.getRequestUserName());
        releaseVersion.setCreatorId(RequestContextUtils.getRequestUserId());
        return releaseVersion;
    }

    @Override
    public void handleToolWithoutToolId(WorkflowExportEntity workflowExportEntity) {
        // 从工作流DSL文件中获取其nodes
        WorkflowVO dsl = workflowExportEntity.getDsl();
        List<WorkflowNodeVO> nodes = dsl.getNodes();

        for (WorkflowNodeVO node : nodes) {
            NodeType nodeType = NodeType.fromType(node.getType());
            if (nodeType == null) {
                log.error("unsupported node type {}", node.getType());
                throw new AgentStudioException(StudioError.WORKFLOW_EXPORT_FILE);
            }
            if (nodeType.equals(PLUGIN)) {
                Map<String, Object> configs = node.getConfigs();

                String tooId = getToolIdFromPlugin(String.valueOf(configs.get("id")));
                if (StringUtils.isNotEmpty(tooId)) {
                    configs.put("tool_id", tooId);
                }
            }
        }
    }

    @Override
    public String getToolIdFromPlugin(String id) {
        PluginEntity plugins = pluginMapper.selectByPrimaryKey(id,null);
        if (ObjectUtils.isEmpty(plugins)) {
            return StringUtils.EMPTY;
        }
        ToolRequestInfo toolRequestInfo;
        String requestInfo = plugins.getRequestInfo();
        if (isNewTool(plugins)) {
            try {
                JsonNode requestInfojsonNode = JsonUtils.JSON_MAPPER.readTree(
                    ObjectUtils.isEmpty(requestInfo) ? "{}" : requestInfo);

                toolRequestInfo = JsonUtils.JSON_MAPPER.treeToValue(requestInfojsonNode, ToolRequestInfo.class);

            } catch (JsonProcessingException e) {
                log.error("Failed to parse requestInfo to ToolInfo {}", e.getMessage());
                throw new AgentStudioException(StudioError.PARSE_REQUEST_TO_TOOL_FAILED);
            }
            List<ToolInfo> tools = toolRequestInfo.getToolsInfoList();
            if (tools.size() == 1) {
                return tools.get(0).getToolId();
            }
            return StringUtils.EMPTY;
        } else {
            return "0";
        }
    }

    @Override
    public void updateName(ToolEntity tool) {
        pluginMapper.updateNameByPrimaryKey(tool.getToolId(), tool.getToolChineseName(), tool.getToolDisplayName());
    }

    public String getPluginId(String toolId) {
        String[] ids = toolId.split("#");
        // 获取 pluginId
        return ids[0];
    }

    @Override
    public void handleToolReleaseVersionId(WorkflowExportEntity workflowExportEntity) {
        // 从工作流DSL文件中获取其nodes
        WorkflowVO dsl = workflowExportEntity.getDsl();
        List<WorkflowNodeVO> nodes = dsl.getNodes();

        for (WorkflowNodeVO node : nodes) {
            NodeType nodeType = NodeType.fromType(node.getType());
            if (nodeType == null) {
                log.error("unsupported node type {}", node.getType());
                throw new AgentStudioException(StudioError.WORKFLOW_EXPORT_FILE);
            }
            if (nodeType.equals(PLUGIN)) {
                Map<String, Object> configs = node.getConfigs();
                if (configs != null && configs.containsKey("version_id") && configs.containsKey("id")) {
                    String toolId = (String) configs.get("id");
                    PluginEntity toolEntity = pluginMapper.selectByPrimaryKey(toolId, null);
                    if (toolEntity != null) {
                        configs.put("version_id", toolEntity.getLastVersionId());
                    }
                }
            }
        }
    }

    @Override
    public List<PluginEntity> getPlugin(String projectId, String workspaceId, List<String> pluginIds) {
        List<PluginEntity> result = pluginMapper.selectByPrimaryIdAndPluginIdsWithSearchCriteria(projectId, workspaceId,
            pluginIds, null);
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        // 共享的插件，校验插件是否被共享
        List<String> originWorkspaceId = shareInnerService.getOriginSharedInfoByResourceId(pluginIds)
            .stream()
            .map(ShareResourceEntity::getWorkspaceId)
            .toList();
        result = pluginMapper.selectByPrimaryIdAndPluginIdsWithSearchCriteria(projectId, null, pluginIds, null);
        return result.stream().filter(plugin -> originWorkspaceId.contains(plugin.getWorkspaceId())).toList();

    }

    @Override
    public PluginEntity getPluginByTraceId(String workspaceId, String traceId) {
        List<PluginEntity> pluginEntities = pluginMapper.selectByTraceIdAndWorkspaceId(null, workspaceId, traceId);
        return pluginEntities.stream().findFirst().orElse(null);
    }

    private Map<String, List<String>> convertToolIds2PluginToolMap(List<String> toolIds) {
        Map<String, List<String>> toolMap = new HashMap<>();
        for (String item : toolIds) {
            String[] parts = item.split("#");
            if (parts.length == 2) {
                String key = parts[0];
                String value = parts[1];

                // 如果键不存在，则初始化列表
                toolMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            } else {
                toolMap.computeIfAbsent(parts[0], k -> new ArrayList<>()).add("0");
            }
        }
        return toolMap;
    }

    @Override
    public ToolEntity getOldToolEntity(String toolId, String projectId, String opProjectId, String versionId) {
        ToolEntity toolEntity;
        if (StringUtils.isNotEmpty(versionId)) {
            // 携带versionId，从OBS读取
            toolEntity = pluginBase.getToolEntityByVersion(toolId, versionId);

        } else {
            // 兼容旧工具，未保存OBS，仍然从数据库读取
            toolEntity = pluginBase.buildToolByPlugin(projectId, null, toolId, "0");
        }
        return toolEntity;
    }

    private boolean isNewTool(String id) {
        return id.contains("#");
    }

    // 对于服务级鉴权，需要把authKey值设置为null，0625修改为"******"与其他服务统一
    public AuthInfo anonymizeAuthInfo(AuthInfo authInfo) {
        if (!Objects.isNull(authInfo) && AuthInfo.ScopeEnum.SERVICE.equals(authInfo.getScope())) {
            authInfo.getAuthKeys().forEach(authKeyInfo -> authKeyInfo.setAuthKey(maskKey(authKeyInfo.getAuthKey())));
        }
        if (!Objects.isNull(authInfo) && AuthInfo.ScopeEnum.HIS_IAM.equals(authInfo.getScope())) {
            authInfo.getHisIamInfo().setIamSecret(maskKey(authInfo.getHisIamInfo().getIamSecret()));
        }
        if (!Objects.isNull(authInfo) && AuthInfo.ScopeEnum.SGOV.equals(authInfo.getScope())) {
            authInfo.getHisSgov().setCredential(maskKey(authInfo.getHisSgov().getCredential()));
        }
        return authInfo;
    }

    private String maskKey(String key) {
        if (StringUtils.isBlank(key)) {
            return CommonConstant.ANONYMIZED_TEXT;
        }
        String decryptedKey = encryptionAdapter.decrypt(key);
        if (StringUtils.isBlank(decryptedKey)) {
            return CommonConstant.ANONYMIZED_TEXT;
        }
        if (key.length() == 1) {
            return decryptedKey.charAt(0) + CommonConstant.ANONYMIZED_TEXT.substring(1);
        }
        return decryptedKey.charAt(0) + CommonConstant.ANONYMIZED_TEXT.substring(1,
            CommonConstant.ANONYMIZED_TEXT.length() - 1) + decryptedKey.charAt(decryptedKey.length() - 1);
    }
}
