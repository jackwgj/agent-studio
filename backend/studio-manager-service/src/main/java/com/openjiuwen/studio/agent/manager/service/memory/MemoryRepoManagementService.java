/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SqlLikeEscapeHelper;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryReposResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryRepositoriesQo;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import com.openjiuwen.studio.agent.manager.dto.MemoryRepoListItem;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.client.MemoryServiceClient;
import com.openjiuwen.studio.agent.manager.rce.models.memory.BatchDeleteMemoryRepositoryRequestBody;
import com.openjiuwen.studio.agent.manager.rce.models.memory.MemoryRepositoryConfig;
import com.openjiuwen.studio.agent.manager.rce.models.memory.SaveOrUpdateMemoryRepositoryRequestBody;
import com.openjiuwen.studio.agent.manager.rce.models.memory.TimeWindow;
import com.openjiuwen.studio.agent.manager.service.IMemoryRepoManagementService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 记忆库管理实现类
 */
@Service
public class MemoryRepoManagementService implements IMemoryRepoManagementService {

    @Autowired
    private MemoryRepoMapper memoryRepoMapper;

    @Autowired
    private MemoryServiceClient memoryServiceClient;

    @Autowired
    private MgObsService mgObsService;

    @Value("${memory.default-icon}")
    private String memoryDefaultIcon;

    @Override
    @Transactional
    public CreateMemoryRepoResponseBody createMemoryRepo(String projectId, String workspaceId, CreateMemoryRepoRequestBody body) {
        String requestIamToken = RequestContextUtils.getRequestAuthToken();
        String language = LanguageUtils.getLanguage();
        // 转换请求体为实体
        MemoryRepoEntity entity = buildMemoryRepoEntity(projectId, workspaceId, body);
        if (StringUtils.isNotEmpty(body.getIcon())) {
            entity.setIcon(body.getIcon());
        }
        // 校验长期策略是否合法，类型唯一/至少一个策略
        checkLongMemoryStrategies(entity.getLongTermMemoryStrategies());

        // 插入数据库
        memoryRepoMapper.insert(entity);

        // 调用memory-service服务，记忆策略
        SaveOrUpdateMemoryRepositoryRequestBody requestBody = buildSaveOrUpdateMemoryRepoRequest(entity);

        memoryServiceClient.saveOrUpdateMemoryRepositoryConfig(requestIamToken, language, projectId, requestBody);
        // 构建返回结果
        CreateMemoryRepoResponseBody response = new CreateMemoryRepoResponseBody();
        response.setMemoryRepoId(entity.getId());
        return response;
    }

    private static SaveOrUpdateMemoryRepositoryRequestBody buildSaveOrUpdateMemoryRepoRequest(MemoryRepoEntity entity) {
        SaveOrUpdateMemoryRepositoryRequestBody requestBody = new SaveOrUpdateMemoryRepositoryRequestBody();
        requestBody.setId(entity.getId());
        MemoryRepositoryConfig config = MemoryRepositoryConfig.builder()
            .longTermMemoryStrategies(entity.getLongTermMemoryStrategies())
            .timeWindow(TimeWindow.builder().conversationRound(entity.getConversationRound()).timeSpan(entity.getTimeSpan()).build())
            .build();
        requestBody.setConfig(config);
        return requestBody;
    }

    private void checkLongMemoryStrategies(List<LongTermMemoryStrategy> longTermMemoryStrategies) {
        if (longTermMemoryStrategies == null || longTermMemoryStrategies.isEmpty()) {
            throw new InvalidParameterException("至少需要一个长期记忆策略");
        }
        // 检查策略类型是否唯一
        Set<LongTermMemoryStrategy.TypeEnum> strategyTypes = new HashSet<>();
        for (LongTermMemoryStrategy strategy : longTermMemoryStrategies) {
            if (strategy == null || strategy.getType() == null) {
                throw new InvalidParameterException("记忆策略类型不能为空");
            }

            if (!strategyTypes.add(strategy.getType())) {
                throw new InvalidParameterException("记忆策略类型重复: " + strategy.getType());
            }
        }
    }

    private static MemoryRepoEntity buildMemoryRepoEntity(String projectId, String workspaceId, CreateMemoryRepoRequestBody body) {
        return MemoryRepoEntity.builder()
            .id(UUID.randomUUID().toString())
            .projectId(projectId)
            .workspaceId(workspaceId)
            .domainId(RequestContextUtils.getRequestUserDomainId())
            .createdUserId(RequestContextUtils.getRequestUserId())
            .createdUserName(RequestContextUtils.getRequestUserName())
            .lastUpdateUserId(RequestContextUtils.getRequestUserId())
            .lastUpdateUserName(RequestContextUtils.getRequestUserName())
            .description(body.getDescription())
            .name(body.getName())
            .longTermMemoryStrategies(body.getLongTermMemoryStrategies())
            .build();
    }

    @Override
    @Transactional
    public DeleteMemoryRepoResponseBody deleteMemoryRepo(String projectId, String memoryRepoId, String workspaceId) {

        // 删除记录
        int result = memoryRepoMapper.deleteById(memoryRepoId);
        String requestIamToken = RequestContextUtils.getRequestAuthToken();
        String language = LanguageUtils.getLanguage();
        BatchDeleteMemoryRepositoryRequestBody requestBody = new BatchDeleteMemoryRepositoryRequestBody();
        if (result == 1) {
            requestBody.setIds(List.of(memoryRepoId));
            memoryServiceClient.batchDeleteMemoryRepositoryConfig(requestIamToken, language, projectId, requestBody);
        }
        DeleteMemoryRepoResponseBody res = new DeleteMemoryRepoResponseBody();

        res.setMemoryRepoId(memoryRepoId);

        return res;
    }

    @Override
    @Transactional
    public ListMemoryReposResponseBody listMemoryRepositories(String projectId, ListMemoryRepositoriesQo query) {
        PageHelper.offsetPage(query.getOffset(), query.getLimit());
        String realQueryName = SqlLikeEscapeHelper.escapeLikeCharacters(query.getName());
        // 设置查询条件
        List<MemoryRepoEntity> memoryRepos = memoryRepoMapper.selectByCondition(
            projectId,
            query.getWorkspaceId(),
            RequestContextUtils.getRequestUserDomainId(),
            query.getIds(),
            realQueryName
        );
        PageInfo<MemoryRepoEntity> memoryRepoPageInfo = new PageInfo<>(memoryRepos);

        // 转换为返回结果
        List<MemoryRepoListItem> items = memoryRepos.stream()
            .map(entity -> {
                MemoryRepoListItem memoryRepoListItem = new MemoryRepoListItem();
                memoryRepoListItem.setName(entity.getName());
                memoryRepoListItem.setMemoryRepoId(entity.getId());
                memoryRepoListItem.setDescription(entity.getDescription());
                memoryRepoListItem.setLastUpdateUserId(entity.getLastUpdateUserId());
                memoryRepoListItem.setLastUpdateUserName(entity.getLastUpdateUserName());
                memoryRepoListItem.setWorkspaceId(entity.getWorkspaceId());
                memoryRepoListItem.setCreatedUserId(entity.getCreatedUserId());
                memoryRepoListItem.setCreatedUserName(entity.getCreatedUserName());
                memoryRepoListItem.setCreateTime(entity.getCreateTime());
                memoryRepoListItem.setUpdateTime(entity.getUpdateTime());
                memoryRepoListItem.setLongTermMemoryStrategies(entity.getLongTermMemoryStrategies());
                memoryRepoListItem.setIcon(StringUtils.isEmpty(entity.getIcon()) ? memoryDefaultIcon : entity.getIcon());
                return memoryRepoListItem;
            })
            .collect(Collectors.toList());

        ListMemoryReposResponseBody response = new ListMemoryReposResponseBody();
        response.setItems(items);
        response.setTotal(memoryRepoPageInfo.getTotal());
        return response;
    }

    @Override
    @Transactional
    public ModifyMemoryRepoResponseBody modifyMemoryRepo(String projectId, String memoryRepoId, String workspaceId, ModifyMemoryRepoRequestBody body) {
        // 查询原记录
        if (body.getLongTermMemoryStrategies() != null && !body.getLongTermMemoryStrategies().isEmpty()) {
            checkLongMemoryStrategies(body.getLongTermMemoryStrategies());
        }
        MemoryRepoEntity entity = buildMemoryRepoEntity(projectId, memoryRepoId, workspaceId, body);

        if (StringUtils.isNotEmpty(body.getIcon())) {
            entity.setIcon(body.getIcon());
        }

        // 更新数据库
        memoryRepoMapper.updateById(entity);

        String requestIamToken = RequestContextUtils.getRequestAuthToken();
        String language = LanguageUtils.getLanguage();
        SaveOrUpdateMemoryRepositoryRequestBody requestBody = buildSaveOrUpdateMemoryRepoRequest(entity);
        memoryServiceClient.saveOrUpdateMemoryRepositoryConfig(requestIamToken, language, projectId, requestBody);

        // 构建返回结果
        ModifyMemoryRepoResponseBody response = new ModifyMemoryRepoResponseBody();
        response.setMemoryRepoId(entity.getId());
        return response;
    }

    private static MemoryRepoEntity buildMemoryRepoEntity(String projectId, String memoryRepoId, String workspaceId, ModifyMemoryRepoRequestBody body) {
        return MemoryRepoEntity.builder()
            .id(memoryRepoId)
            .projectId(projectId)
            .workspaceId(workspaceId)
            .domainId(RequestContextUtils.getRequestUserDomainId())
            .description(body.getDescription())
            .name(body.getName())
            .longTermMemoryStrategies(body.getLongTermMemoryStrategies())
            .lastUpdateUserId(RequestContextUtils.getRequestUserId())
            .lastUpdateUserName(RequestContextUtils.getRequestUserName())
            .build();
    }

    @Override
    @Transactional
    public ShowMemoryRepoResponseBody showMemoryRepo(String projectId, String memoryRepoId, String workspaceId) {
        MemoryRepoEntity entity = memoryRepoMapper.selectById(memoryRepoId);

        if (entity == null) {
            throw new AgentStudioException(StudioError.MEMORY_REPO_NOT_EXIST);
        }
        return convertToShowResponse(entity);
    }

    /**
     * 转换为展示响应对象
     */
    private ShowMemoryRepoResponseBody convertToShowResponse(MemoryRepoEntity entity) {
        ShowMemoryRepoResponseBody response = new ShowMemoryRepoResponseBody();
        response.setName(entity.getName());
        response.setMemoryRepoId(entity.getId());
        response.setDescription(entity.getDescription());
        response.setCreatedUserId(entity.getCreatedUserId());
        response.setCreatedUserName(entity.getCreatedUserName());
        response.setCreateTime(entity.getCreateTime());
        response.setLastUpdateUserId(entity.getLastUpdateUserId());
        response.setLastUpdateUserName(entity.getLastUpdateUserName());
        response.setUpdateTime(entity.getUpdateTime());
        response.setLongTermMemoryStrategies(entity.getLongTermMemoryStrategies());
        response.setIcon(StringUtils.isEmpty(entity.getIcon()) ? memoryDefaultIcon : entity.getIcon());
        return response;
    }
}
