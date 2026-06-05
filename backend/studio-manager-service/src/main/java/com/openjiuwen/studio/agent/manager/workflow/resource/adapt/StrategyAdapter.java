/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.adapt;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.ModelExportEntity;
import com.openjiuwen.studio.agent.manager.entity.ModelStrategyExportEntity;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceData;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;
import com.openjiuwen.studio.agent.manager.enums.ResourceTypeEnum;
import com.openjiuwen.studio.agent.manager.mapper.md.RouterStrategyMapper;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.service.md.RouterStrategyMgmtService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportInfo;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResourceUnit;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResp;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ExportResult;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportCheckResult;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportExportStatusEnum;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportInfo;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportResourceResult;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("STRATEGY")
@Slf4j
public class StrategyAdapter extends ResourceAdapter {

    @Autowired
    private RouterStrategyMgmtService strategyMgmtService;

    @Autowired
    private ModelServiceManager modelServiceManager;

    @Autowired
    private RouterStrategyMapper routerStrategyMapper;

    @Autowired
    private I18nUtil i18nUtil;

    @Override
    public ExportResp parseExport(List<ExportResourceUnit> exportResources) {
        if (CollectionUtils.isEmpty(exportResources)) {
            log.info("strategy resource input is null");
            return null;
        }

        List<String> strategyInputIds = exportResources.stream().map(ExportResourceUnit::getResourceId).toList();

        List<ModelStrategyExportEntity> modelStrategyEntities = strategyMgmtService.buildModelStrategyExport(
            RequestContextUtils.getRequestProjectId(), RequestContextUtils.getRequestWorkspaceId(), strategyInputIds);
        ExportResp exportResp = new ExportResp();
        List<ModelExportEntity> modelExportEntities = modelStrategyEntities.stream()
            .map(ModelStrategyExportEntity::getModelExportEntity)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        Map<String, List<String>> modelParentIdMap = getModelParentMap(modelStrategyEntities);
        // 构造导出数据
        List<ModelServiceBase> modelServiceBases = com.openjiuwen.studio.agent.manager.workflow.resource.adapt.ModelAdapter.buildModelProviderInfos(modelParentIdMap, exportResp,
            modelExportEntities);
        // 构造策略中模型导出结果
        exportResp.getExportResults().addAll(getExportModelResults(modelServiceBases));

        List<RouterStrategyEntity> routerStrategies = modelStrategyEntities.stream()
            .map(ModelStrategyExportEntity::getRouterStrategyEntityMetadata)
            .toList();
        List<ExportResult> exportStrategyResults = getExportStrategyResults(exportResources, routerStrategies);
        if (CollectionUtils.isNotEmpty(exportStrategyResults)) {
            exportResp.getExportResults().addAll(exportStrategyResults);
        }
        List<ExportInfo> exportInfos = new ArrayList<>();
        Map<String, List<String>> parentIdMap = getParentIdMap(exportResources);
        for (RouterStrategyEntity strategyEntity : routerStrategies) {
            ExportInfo exportInfo = new ExportInfo();
            exportInfo.setMetadata(strategyEntity);
            exportInfo.setResourceId(strategyEntity.getId());
            exportInfo.setParents(parentIdMap.get(strategyEntity.getId()));
            exportInfo.setResourceType(ResourceTypeEnum.STRATEGY.toString());
            exportInfo.setResourceName(strategyEntity.getStrategyName());
            exportInfos.add(exportInfo);
        }
        if (CollectionUtils.isEmpty(exportResp.getExportInfos())) {
            exportResp.setExportInfos(exportInfos);
        } else {
            exportResp.getExportInfos().addAll(exportInfos);
        }
        return exportResp;
    }

    private static Map<String, List<String>> getModelParentMap(List<ModelStrategyExportEntity> modelExportEntities) {
        Map<String, List<String>> modelParentMap = new HashMap<>();
        modelExportEntities.forEach(p -> {
            List<ModelServiceData> modelServiceDataList = p.getModelExportEntity()
                .stream()
                .map(ModelExportEntity::getModelMetadata)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .toList();
            List<String> modelIds = modelServiceDataList.stream()
                .map(ModelServiceData::getId)
                .distinct()
                .collect(Collectors.toList());
            String strategyId = p.getRouterStrategyEntityMetadata().getId();
            modelIds.forEach(modelId -> {
                if (modelParentMap.get(modelId) != null) {
                    modelParentMap.get(modelId).add(strategyId);
                } else {
                    modelParentMap.put(modelId, Arrays.asList(strategyId));
                }
            });
        });
        return modelParentMap;
    }


    private List<ExportResult> getExportModelResults(List<ModelServiceBase> modelExportEntities) {
        List<ExportResult> exportResults = new ArrayList<>();
        for (ModelServiceBase modelServiceBase : modelExportEntities) {
            ExportResult result = new ExportResult();
            result.setResourceId(modelServiceBase.getId());
            result.setResourceName(modelServiceBase.getModelName());
            result.setResourceType(ResourceTypeEnum.MODEL.toString());
            result.setStatus(ImportExportStatusEnum.SUCCESS);
            exportResults.add(result);
        }
        return exportResults;
    }

    private List<ExportResult> getExportStrategyResults(List<ExportResourceUnit> exportResources,
        List<RouterStrategyEntity> routerStrategies) {
        List<String> validStrategyIds = routerStrategies.stream().map(RouterStrategyEntity::getId).toList();
        List<ExportResult> exportResults = new ArrayList<>();
        for (ExportResourceUnit exportResourceUnit : exportResources) {
            ExportResult result = new ExportResult();
            result.setResourceId(exportResourceUnit.getResourceId());
            result.setResourceName(exportResourceUnit.getResourceName());
            result.setResourceType(exportResourceUnit.getResourceType());
            result.setStatus(ImportExportStatusEnum.SUCCESS);
            if (!validStrategyIds.contains(exportResourceUnit.getResourceId())) {
                result.setReason(
                    i18nUtil.getMessage(StudioError.EXPORT_RESOURCE_NOT_EXISTS, ResourceTypeEnum.STRATEGY.toString()));
                result.setStatus(ImportExportStatusEnum.FAILED);
            }
            exportResults.add(result);
        }
        return exportResults;
    }

    @Override
    public void checkBeforeImport(ImportCheckResult importCheckResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        RouterStrategyEntity routerStrategy = JsonUtils.objectToClassType(importInfo.getMetadata(),
            RouterStrategyEntity.class);
        importCheckResult.setDescription(routerStrategy.getStrategyDescription());

        RouterStrategyEntity strategy = getStrategyByTraceId(importInfo.getTargetProjectId(),
            importInfo.getTargetWorkspaceId(), routerStrategy.getTraceId());
        importCheckResult.setStatus(strategy != null);
        importCheckResult.setImportDesc(getImportDescNoVersion(strategy));
    }

    private void checkMetadata(ImportInfo importInfo) {
        if (importInfo.getMetadata() == null) {
            throw new AgentStudioException(StudioError.FILE_LACKS_SOURCE_DATA);
        }
    }

    private RouterStrategyEntity getStrategyByTraceId(String projectId, String workspaceId, String traceId) {
        List<RouterStrategyEntity> StrategyList = routerStrategyMapper.selectInfoByStrategyName(projectId, workspaceId,
            null);
        return StrategyList.stream().filter(v -> Strings.CS.equals(v.getTraceId(), traceId)).findFirst().orElse(null);
    }

    @Override
    public void parseImport(ImportResourceResult importResourceResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        RouterStrategyEntity strategy = JsonUtils.objectToClassType(importInfo.getMetadata(),
            RouterStrategyEntity.class);
        setImportResultDesc(importResourceResult, null, strategy.getStrategyDescription());
        handleStrategy(importInfo, strategy, importResourceResult);
    }

    private void handleStrategy(ImportInfo importInfo, RouterStrategyEntity strategy, ImportResourceResult result) {
        RouterStrategyEntity existingStrategy = getStrategyByTraceId(importInfo.getTargetProjectId(),
            importInfo.getTargetWorkspaceId(), strategy.getTraceId());
        updateMetadata(importInfo, strategy);
        if (existingStrategy == null) {
            setName(strategy);
            result.setNewName(strategy.getStrategyName());
            // 若当前空间资源不存在，且该资源id已存在，则更换id
            if (routerStrategyMapper.selectInfoById(strategy.getId()) != null) {
                strategy.setId(UUID.randomUUID().toString());
                result.setNewId(strategy.getId());
            }
            routerStrategyMapper.insert(strategy);
        } else {
            if (!Strings.CS.equals(existingStrategy.getId(), strategy.getId())) {
                strategy.setId(existingStrategy.getId());
                strategy.setStrategyName(existingStrategy.getStrategyName());
                result.setNewId(existingStrategy.getId());
                result.setNewName(existingStrategy.getStrategyName());
            }
            routerStrategyMapper.update(strategy);
        }
        modelServiceManager.saveModelInfoToObsAndRedis("router", strategy.getId(), strategy);
    }

    private void updateMetadata(ImportInfo importInfo, RouterStrategyEntity strategy) {
        strategy.setProjectId(importInfo.getTargetProjectId());
        strategy.setWorkspaceId(importInfo.getTargetWorkspaceId());
        strategy.setCreatedByUserName(importInfo.getCreator());
        strategy.setCreatedDate(new Date().getTime());
        strategy.setLastUpdatedByUserName(importInfo.getCreatorId());
        strategy.setLastUpdatedDate(new Date().getTime());
        strategy.setDomainId(importInfo.getTargetDomainId());

    }

    private void setName(RouterStrategyEntity strategy) {
        String name = strategy.getStrategyName();
        List<RouterStrategyEntity> strategysByWorkspaceId = routerStrategyMapper.selectInfoByStrategyName(
            strategy.getProjectId(), strategy.getWorkspaceId(), null);
        Set<String> currentNameSet = strategysByWorkspaceId.stream()
            .map(RouterStrategyEntity::getStrategyName)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        List<RouterStrategyEntity> sameNameList = strategysByWorkspaceId.stream()
            .filter(v -> Strings.CS.equals(v.getStrategyName(), name))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sameNameList)) {
            return;
        }
        // 用户在当前环境中已存在该MCP服务，且name未改变，不会冲突
        if (sameNameList.stream().filter(v -> Strings.CS.equals(v.getTraceId(), strategy.getTraceId())).count() > 0) {
            return;
        }
        // 设置唯一资源名称
        strategy.setStrategyName(getFormatName(name, currentNameSet));
    }

}
