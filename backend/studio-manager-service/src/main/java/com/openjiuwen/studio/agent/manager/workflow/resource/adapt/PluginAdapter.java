/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.adapt;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginVO;
import com.openjiuwen.studio.agent.manager.enums.ResourceTypeEnum;
import com.openjiuwen.studio.agent.manager.enums.ToolType;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.ShareResourceManagerService;
import com.openjiuwen.studio.agent.manager.service.adapter.AgentPluginAdapter;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportInfo;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResourceUnit;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResp;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResult;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportCheckResult;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportExportStatusEnum;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportInfo;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportResourceResult;
import com.twelvemonkeys.util.LinkedSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("TOOL")
@Slf4j
public class PluginAdapter extends ResourceAdapter {
    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private MgObsService obsService;

    @Autowired
    private IPlugin pluginService;

    @Autowired
    private IPluginBase pluginBase;

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private ShareResourceManagerService shareResourceManagerService;

    @Autowired
    private ReleaseVersionMapper releaseVersionMapper;

    @Autowired
    private AgentPluginAdapter agentPluginAdapter;

    @Override
    public ExportResp parseExport(List<ExportResourceUnit> exportResources) {
        if (CollectionUtils.isEmpty(exportResources)) {
            log.info("plugin resource input is null");
            return null;
        }
        List<String> tmpIds = exportResources.stream().map(ExportResourceUnit::getResourceId).toList();
        // 插件和工具的map关系
        Map<String, List<String>> plugin2ToolId = generatePlugin2ToolId(tmpIds);
        // 所有插件id
        List<String> inputPluginIds = plugin2ToolId.keySet().stream().toList();
        IPlugin pluginAdapter = SpringBeanUtils.getBean(IPlugin.class);
        List<PluginEntity> plugins = pluginAdapter.getPlugin(null, null, inputPluginIds);
        // 当前插件id和父智能体的关系
        Map<String, List<String>> parentIdMap = getParentIdMapForPlugin(exportResources);
        // id和插件对象的映射关系
        Map<String, PluginEntity> pluginEntityMap = plugins.stream()
            .collect(Collectors.toMap(PluginEntity::getPluginId, p -> p));
        // 构造导出结果
        ExportResp exportResp = new ExportResp();
        List<ExportResult> exportResults = getExportResults(exportResources, plugins);
        exportResp.setExportResults(exportResults);

        // 获取版本信息
        List<ReleaseVersion> releaseVersions = getReleaseVersionsForPlugin(plugins, exportResources, exportResults);
        // 构造导出数据
        List<ExportInfo> exportInfos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(releaseVersions)) {
            MgObsService obsService = SpringBeanUtils.getBean(MgObsService.class);
            for (ReleaseVersion pluginRelease : releaseVersions) {
                ExportInfo exportInfo = new ExportInfo();
                PluginEntity pluginEntity = pluginBase.getPluginEntityByVersion(pluginRelease.getAppId(),
                    pluginRelease.getVersionId());
                exportInfo.setDsl(obsService.downloadObsFile(pluginRelease.getDslPath()));
                exportInfo.setResourceId(pluginRelease.getAppId());
                exportInfo.setResourceType(ResourceTypeEnum.TOOL.toString());
                exportInfo.setResourceName(pluginEntity.getPluginDisplayName());
                exportInfo.setMetadata(buildMetadataForPlugin(pluginEntityMap.get(pluginRelease.getAppId()),
                    plugin2ToolId.get(pluginRelease.getAppId())));
                exportInfo.setReleaseVersion(pluginRelease);
                exportInfo.setShareInfo(shareResourceManagerService.exportShareInfo(pluginRelease.getAppId()));
                exportInfo.setResourceLevel(2);
                exportInfo.setParents(parentIdMap.get(pluginRelease.getAppId()));
                exportInfos.add(exportInfo);
                setSuccessInfo(exportResults, pluginRelease.getAppId());
            }
        }

        // 预置数据
        List<PluginEntity> innerPlugins = plugins.stream()
            .filter(p -> Strings.CS.equals(p.getType(), ToolType.INNER.type))
            .toList();
        if (CollectionUtils.isNotEmpty(innerPlugins)) {
            for (PluginEntity innerPlugin : innerPlugins) {
                ExportInfo exportInfo = new ExportInfo();
                exportInfo.setDsl(innerPlugin);
                exportInfo.setResourceId(innerPlugin.getPluginId());
                exportInfo.setResourceType(ResourceTypeEnum.TOOL.toString());
                exportInfo.setResourceName(innerPlugin.getPluginDisplayName());
                exportInfo.setMetadata(buildMetadataForPlugin(pluginEntityMap.get(innerPlugin.getPluginId()),
                    plugin2ToolId.get(innerPlugin.getPluginId())));
                exportInfo.setResourceLevel(2);
                exportInfo.setParents(parentIdMap.get(innerPlugin.getPluginId()));
                exportInfos.add(exportInfo);
                setSuccessInfo(exportResults, innerPlugin.getPluginId());
            }
        }

        exportResp.setExportInfos(exportInfos);
        return exportResp;
    }

    private PluginEntity buildMetadataForPlugin(PluginEntity pluginRelease, List<String> toolIds) {
        PluginVO pluginVO = new PluginVO();
        BeanUtils.copyProperties(pluginRelease, pluginVO);
        pluginVO.setToolIds(toolIds);
        return pluginVO;
    }

    private Map<String, List<String>> generatePlugin2ToolId(List<String> inputPluginsId) {
        // 创建一个 Map，其中 key 为 String，值为 List<String>
        Map<String, List<String>> resultMap = new HashMap<>();

        // 遍历数组
        for (String item : inputPluginsId) {
            // 分割字符串，获取 key 和 value
            String key = getPluginId(item);
            String value = getToolId(item);

            // 如果 key 不在 Map 中，则初始化为空列表
            resultMap.putIfAbsent(key, new ArrayList<>());

            // 将 value 添加到对应 key 的列表中
            resultMap.get(key).add(value);
        }

        return resultMap;
    }

    private List<ExportResult> getExportResults(List<ExportResourceUnit> exportResources,
        List<PluginEntity> pluginEntities) {
        Set<String> tmpPluginIds = new LinkedSet<>();
        List<String> pluginIds = pluginEntities.stream().map(PluginEntity::getPluginId).toList();
        List<ExportResult> exportResults = new ArrayList<>();
        for (ExportResourceUnit exportResourceUnit : exportResources) {
            if (tmpPluginIds.contains(getPluginId(exportResourceUnit.getResourceId()))) {
                continue;
            }
            ExportResult result = getExportResult(exportResourceUnit, pluginIds);
            tmpPluginIds.add(getPluginId(exportResourceUnit.getResourceId()));
            exportResults.add(result);
        }
        return exportResults;
    }

    private @NotNull ExportResult getExportResult(ExportResourceUnit exportResourceUnit, List<String> pluginIds) {
        ExportResult result = new ExportResult();
        result.setResourceId(exportResourceUnit.getResourceId());
        result.setResourceName(exportResourceUnit.getResourceName());
        result.setResourceType(exportResourceUnit.getResourceType());
        result.setResourceVersion(exportResourceUnit.getResourceVersion());
        result.setStatus(ImportExportStatusEnum.SUCCESS);
        if (!pluginIds.contains(getPluginId(exportResourceUnit.getResourceId()))) {
            result.setReason("插件不存在");
            result.setStatus(ImportExportStatusEnum.FAILED);
        }
        return result;
    }

    @Override
    public void checkBeforeImport(ImportCheckResult importCheckResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        PluginEntity pluginEntity;
        try {
            pluginEntity = JsonUtils.objectToClassType(importInfo.getMetadata(), PluginEntity.class);
        } catch (Exception e) {
            log.error("plugin parse error", e);
            importCheckResult.setStatus(false);
            return;
        }
        if (ObjectUtils.isEmpty(pluginEntity)) {
            log.error("plugin parse error");
            importCheckResult.setStatus(false);
            return;
        }
        importCheckResult.setDescription(
            StringUtils.isEmpty(pluginEntity.getPluginDesc()) ? StringUtils.EMPTY : pluginEntity.getPluginDesc());
        // 预制资源不可导入
        if (ToolType.INNER.type.equals(pluginEntity.getType())) {
            importCheckResult.setProhibited(true);
            importCheckResult.setStatus(true);
        }
        PluginEntity mcpEntity = getPluginTraceId(importInfo.getTargetWorkspaceId(), pluginEntity.getTraceId());
        importCheckResult.setStatus(mcpEntity != null);
        importCheckResult.setImportDesc(getImportDescNoVersion(mcpEntity));
    }

    private void checkMetadata(ImportInfo importInfo) {
        if (importInfo.getMetadata() == null) {
            throw new AgentStudioException(StudioError.FILE_LACKS_SOURCE_DATA);
        }
    }

    private PluginEntity getPluginTraceId(String workspaceId, String traceId) {
        return pluginService.getPluginByTraceId(workspaceId, traceId);
    }

    @Override
    public void parseImport(ImportResourceResult importResourceResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        PluginEntity pluginEntity;

        pluginEntity = JsonUtils.objectToClassType(importInfo.getMetadata(), PluginEntity.class);
        if (ObjectUtils.isEmpty(pluginEntity)) {
            return;
        }
        setImportResultDesc(importResourceResult, pluginEntity.getVersionId(), pluginEntity.getPluginDesc());
        // 预置资源不允许导入
        if (ToolType.INNER.type.equals(pluginEntity.getType())) {
            handlePlatformResource(importResourceResult);
        } else {
            // 处理自定义资源
            handleWidgetsPlugin(importInfo, pluginEntity, importResourceResult);
        }
    }

    private void handleWidgetsPlugin(ImportInfo importInfo, PluginEntity pluginEntity, ImportResourceResult result) {
        PluginEntity existPlugin = getPluginTraceId(importInfo.getTargetWorkspaceId(), pluginEntity.getTraceId());
        updateMetadata(importInfo, pluginEntity);
        if (existPlugin == null) {
            setPluginName(importInfo.getTargetWorkspaceId(), pluginEntity);
            // 若当前空间资源不存在，且该资源id已存在，则更换id
            if (pluginService.getPlugin(null, null, Collections.singletonList(pluginEntity.getPluginId())) != null) {
                pluginEntity.setPluginId(UUID.randomUUID().toString());
                result.setNewId(pluginEntity.getPluginId());
            }
            if (pluginEntity.getAuthInfo() != null && pluginEntity.getAuthInfo().getScope() != null) {
                pluginEntity.setAuthInfo(new AuthInfo());
                result.setConfigDisplay(true);
            }
            pluginMapper.insert(pluginEntity);
        } else {
            pluginEntity.setPluginId(existPlugin.getPluginId());
            pluginMapper.updateByPrimaryKeySelective(pluginEntity);
            result.setNewId(existPlugin.getPluginId());
        }
        importToolVersion(importInfo.getReleaseVersion(), pluginEntity, result);
    }

    // 导入插件版本
    private boolean importToolVersion(ReleaseVersion releaseVersion, PluginEntity toolEntity, ImportResourceResult result) {
        String toolId = getPluginId(toolEntity.getPluginId());
        String versionId = releaseVersion.getVersionId();
        ReleaseVersion existVersion = releaseVersionMapper.selectByAppIdAndVersionId(toolId, versionId);
        result.setVersion(versionId);
        if (existVersion != null) {
            return true;
        }

        try {
            releaseVersion = releaseToolVersionHandler(toolId, releaseVersion);
            releaseVersionMapper.insert(releaseVersion);
            pluginService.updateOldTool(null, versionId, getPluginId(toolEntity.getPluginId()));
        } catch (Exception e) {
            log.error("import tool:{}, version:{} failed {}", toolId, versionId, e.getMessage());
            return false;
        }
        return true;
    }

    public ReleaseVersion releaseToolVersionHandler(String toolId, ReleaseVersion releaseVersion) {
        PluginEntity toolEntity = pluginMapper.selectByPrimaryKey(toolId, null);
        toolId = toolEntity.getPluginId();
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
        releaseVersion.setAppType(CommonConstant.TOOL_TYPE);
        releaseVersion.setStatus(CommonConstant.NORMAL);
        releaseVersion.setAppId(toolEntity.getPluginId());
        releaseVersion.setCreator(RequestContextUtils.getRequestUserName());
        releaseVersion.setCreatorId(RequestContextUtils.getRequestUserId());
        return releaseVersion;
    }

    private void updateMetadata(ImportInfo importInfo, PluginEntity pluginEntity) {
        pluginEntity.setProjectId(importInfo.getTargetProjectId());
        pluginEntity.setWorkspaceId(importInfo.getTargetWorkspaceId());
        pluginEntity.setCreatorId(importInfo.getCreatorId());
        pluginEntity.setCreatedOn(new Timestamp(new Date().getTime()));
        pluginEntity.setUpdatedOn(new Timestamp(new Date().getTime()));
        pluginEntity.setDomainId(importInfo.getTargetDomainId());
    }

    /**
     * 保障mcp名称唯一
     * @param targetWorkspaceId
     * @param pluginEntity
     */
    private void setPluginName(String targetWorkspaceId, PluginEntity pluginEntity) {
        int existChineseName = pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(
            pluginEntity.getPluginChineseName(), targetWorkspaceId, pluginEntity.getPluginId());
        int existDisplayName = pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(
            pluginEntity.getPluginDisplayName(), targetWorkspaceId, pluginEntity.getPluginId());
        if (existChineseName > 0) {
            int seq = 1;
            while (true) {
                String toolChineseName = subName(pluginEntity.getPluginChineseName(), CommonConstant.NAME_MAX_LEN);
                String candidateName = toolChineseName + "_" + seq;
                if (pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(candidateName, targetWorkspaceId,
                    pluginEntity.getPluginId()) == 0) {
                    pluginEntity.setPluginChineseName(candidateName);
                    break;
                }
                seq++;
            }
        }
        if (existDisplayName > 0) {
            int seq = 1;
            while (true) {
                String toolDisplayName = subName(pluginEntity.getPluginDisplayName(), CommonConstant.NAME_MAX_LEN);
                String candidateName = toolDisplayName + "_" + seq;
                if (pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(candidateName, targetWorkspaceId,
                    pluginEntity.getPluginId()) == 0) {
                    pluginEntity.setPluginDisplayName(candidateName);
                    break;
                }
                seq++;
            }
        }
    }

    private String getPluginId(String toolId) {
        String[] ids = toolId.split("#");
        // 获取 pluginId
        return ids[0];
    }

    private String getToolId(String toolId) {
        String[] ids = toolId.split("#");
        // 获取 pluginId
        return ids.length > 1 ? ids[1] : pluginService.getToolIdFromPlugin(ids[0]); // 如果存在 toolId, 否则为从数据库获取
    }

    public List<ReleaseVersion> getReleaseVersionsForPlugin(List<PluginEntity> pluginEntities,
        List<ExportResourceUnit> exportResources, List<ExportResult> exportResults) {
        List<String> pluginIds = pluginEntities.stream().map(PluginEntity::getPluginId).toList();
        // 查询导出资源版本dsl
        List<ExportResourceUnit> exportResourceUnits = exportResources.stream()
            .filter(p -> pluginIds.contains(getPluginId(p.getResourceId())))
            .toList();
        if (CollectionUtils.isEmpty(exportResourceUnits)) {
            log.info("all export workflow resource failed");
            return null;
        }
        List<ReleaseVersion> inputs = convert2ReleaseParam(exportResourceUnits, exportResults);
        if (CollectionUtils.isEmpty(inputs)) {
            return new ArrayList<>();
        }
        return releaseVersionMapper.queryByWfVersions(inputs);
    }

    /**
     * 获取版本信息，插件可能有重复，删除重复插件
     *
     * @param successExportResults
     * @param exportResults
     * @return
     */
    private List<ReleaseVersion> convert2ReleaseParam(List<ExportResourceUnit> successExportResults,
        List<ExportResult> exportResults) {
        List<String> tmpPluginIds = new ArrayList<>();
        List<ReleaseVersion> result = new ArrayList<>();

        for (ExportResourceUnit exportResourceUnit : successExportResults) {
            String pluginId = getPluginId(exportResourceUnit.getResourceId());
            if (tmpPluginIds.contains(pluginId)) {
                continue;
            }

            String versionId = agentPluginAdapter.getToolVersionFromAgent(exportResourceUnit);
            if (StringUtils.isEmpty(versionId)) {
                setFailedInfo(exportResults, pluginId);
                continue;
            }

            // 创建新实例
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setAppId(pluginId);
            releaseVersion.setVersionId(versionId);
            tmpPluginIds.add(pluginId);
            result.add(releaseVersion);
        }
        return result;
    }

    private void setSuccessInfo(List<ExportResult> exportResults, String pluginId) {
        for (ExportResult exportResult : exportResults) {
            if (!Strings.CS.equals(getPluginId(exportResult.getResourceId()), pluginId)) {
                continue;
            }
            exportResult.setReason(null);
            exportResult.setStatus(ImportExportStatusEnum.SUCCESS);
        }
    }

    private void setFailedInfo(List<ExportResult> exportResults, String pluginId) {
        for (ExportResult exportResult : exportResults) {
            if (!Strings.CS.equals(getPluginId(exportResult.getResourceId()), pluginId)) {
                continue;
            }
            exportResult.setReason("插件未发布");
            exportResult.setStatus(ImportExportStatusEnum.FAILED);
        }
    }

    public Map<String, List<String>> getParentIdMapForPlugin(List<ExportResourceUnit> exportResources) {
        Map<String, List<String>> groupedMap = new HashMap<>();
        for (ExportResourceUnit unit : exportResources) {
            String key = getPluginId(unit.getResourceId());
            String value = unit.getParentResourceId();
            groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return groupedMap;
    }
}
