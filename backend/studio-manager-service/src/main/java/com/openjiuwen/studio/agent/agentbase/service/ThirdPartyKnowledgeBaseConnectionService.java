/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnection;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorAbilityEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectionStatus;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorAbilityMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorMapper;
import com.openjiuwen.studio.agent.agentbase.model.AbilityDetail;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;
import com.openjiuwen.studio.agent.agentbase.service.validate.KnowledgeBaseConnectionValidateService;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.IconValidator;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.foundation.base.utils.UUIDGenerator;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SqlLikeEscapeHelper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;
import com.openjiuwen.studio.agent.foundation.connection.ConnectionParamType;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.KnowledgeBaseConnectorContext;
import com.openjiuwen.studio.agent.foundation.connection.ability.AbilityProcessor;
import com.openjiuwen.studio.agent.foundation.connection.ability.AbilityProcessorFactory;
import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;
import com.openjiuwen.studio.agent.foundation.connection.constants.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;
import com.openjiuwen.studio.agent.manager.dto.AbilityInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseConnectorDetail;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsResponse;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ParamDefinitionInfo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesQo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ThirdPartyKnowledgeBaseConnectionDetail;
import com.openjiuwen.studio.agent.manager.dto.ThirdPartyKnowledgeBaseConnectionListItem;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.service.IThirdPartyKnowledgeBaseConnectionManagementService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 第三方知识库连接管理，只用于第三方知识库连接相关操作。
 */
@Slf4j
@Service
public class ThirdPartyKnowledgeBaseConnectionService implements IThirdPartyKnowledgeBaseConnectionManagementService {

    private final KnowledgeBaseConnectorContext knowledgeBaseConnectorContext;

    private final KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    private final AbilityProcessorFactory abilityProcessorFactory;

    @Value("${knowledge.default-icon}")
    private String defaultIcon;

    @Value("${env.type}")
    private String deployMode;

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Value("${knowledge.icon-max-size:200}")
    private long iconMaxSize;

    @Value("${knowledge.icon-type:png,jpg,jpeg,gif}")
    private String iconType;

    final KnowledgeBaseConnectionMapper connectionMapper;

    final KnowledgeBaseConnectorMapper connectorMapper;

    final KnowledgeBaseConnectorAbilityMapper connectorAbilityMapper;

    final KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService;

    final ResourceValidateService resourceValidateService;

    private final com.openjiuwen.studio.agent.agentbase.service.I18nTransService i18nTransService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ThirdPartyKnowledgeBaseConnectionService(KnowledgeBaseConnectionMapper connectionMapper,
        KnowledgeBaseConnectorMapper connectorMapper,
        KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService,
        KnowledgeBaseConnectorContext knowledgeBaseConnectorContext,
        KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        KnowledgeBaseConnectorAbilityMapper connectorAbilityMapper, ResourceValidateService resourceValidateService,
        I18nTransService i18nTransService, AbilityProcessorFactory abilityProcessorFactory) {
        this.connectionMapper = connectionMapper;
        this.connectorMapper = connectorMapper;
        this.knowledgeBaseConnectionValidateService = knowledgeBaseConnectionValidateService;
        this.connectorAbilityMapper = connectorAbilityMapper;
        this.resourceValidateService = resourceValidateService;
        this.knowledgeBaseConnectorContext = knowledgeBaseConnectorContext;
        this.knowledgeBaseConnectionMapper = knowledgeBaseConnectionMapper;
        this.i18nTransService = i18nTransService;
        this.abilityProcessorFactory = abilityProcessorFactory;
    }

    @Override
    @Transactional
    public CreateThirdPartyKnowledgeBaseConnectionResponse createThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, CreateThirdPartyKnowledgeBaseConnectionRequestBody request) {
        log.info("operation log projectId: {}, userId:{}, createThirdPartyKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        // 校验参数与连接器中定义的参数是否一致并加密
        List<ConnectionParamInfo> connectionParamInfos = knowledgeBaseConnectionValidateService.checkAndEncryptedParams(
            request.getConnectorId(), request.getParams());
        request.setParams(connectionParamInfos);
        // 校验配额
        resourceValidateService.resourceQuotaCheck(RequestContextUtils.getRequestUserDomainId(),
            ResourceType.THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION, 1);
        // 重名校验
        knowledgeBaseConnectionValidateService.checkThirdPartyKnowledgeBaseConnectionName(workspaceId,
            request.getName(), null);
        // 入库
        KnowledgeBaseConnectionEntity entity = toKnowledgeBaseConnectionEntity(projectId, workspaceId, request);
        // 相同连接信息校验
        if (knowledgeBaseConnectionValidateService.checkConnectionUnique(workspaceId, entity)) {
            log.error("connection info {} already exist", entity.getParams());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_CONNECTION_ALREADY_EXIST);
        }
        if (StringUtils.isBlank(request.getIcon())) {
            entity.setIcon(defaultIcon);
        } else {
            IconValidator.validate(request.getIcon(), iconMaxSize, iconType);
            entity.setIcon(request.getIcon());
        }
        try {
            connectionMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            log.error("Create third-party knowledge base connection", e);
            throw new AgentBaseException(ErrorCode.RESOURCE_ID_DUPLICATE, e);
        }
        return new CreateThirdPartyKnowledgeBaseConnectionResponse().setId(entity.getId());
    }

    @Override
    public CommonDeleteRsp deleteThirdPartyKnowledgeBaseConnection(String projectId, String workspaceId,
        String knowledgeBaseConnectionId) {
        log.info("operation log projectId: {}, userId:{}, deleteThirdPartyKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        KnowledgeBaseConnectionEntity connectionEntity
            = knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(workspaceId,
            knowledgeBaseConnectionId);
        if (connectionEntity.getStatus().equals(KnowledgeBaseConnectionStatus.OPEN)) {
            log.error("can not delete third party knowledge base,because the status is open");
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_CONNECTION_DELETE_FOR_OPEN);
        }
        connectionMapper.delete(knowledgeBaseConnectionId);
        return new CommonDeleteRsp().setId(knowledgeBaseConnectionId);
    }

    @Override
    public ListThirdPartyKnowledgeBaseConnectorsResponseBody listThirdPartyKnowledgeBaseConnectors(String projectId,
        ListThirdPartyKnowledgeBaseConnectorsQo body) {
        List<KnowledgeBaseConnectorEntity> connectorEntities = connectorMapper.listKnowledgeBaseConnectors(deployMode);

        // 筛选支持的connector类型
        connectorEntities = connectorEntities.stream()
            .filter(connector -> Arrays.stream(ConnectorTypeEnum.values())
                .map(ConnectorTypeEnum::getValue)
                .anyMatch(value -> value.equals(connector.getId())))
            .collect(Collectors.toList());

        List<KnowledgeBaseConnectorDetail> connectorDetails = connectorEntities.stream()
            .map(this::convertConnectorEntityToConnectorItem)
            .toList();

        // 国际化处理 Connector 信息
        translateConnectors(connectorDetails);

        return new ListThirdPartyKnowledgeBaseConnectorsResponseBody().setConnectors(connectorDetails)
            .setTotal((long) connectorDetails.size());
    }

    @Override
    public ShowKnowledgeBaseConnectorAbilitiesResponseBody showKnowledgeBaseConnectorAbilities(String projectId,
        ShowKnowledgeBaseConnectorAbilitiesQo showKnowledgeBaseConnectorAbilitiesQo) {
        List<AbilityInfo> abilitiesForResult = new ArrayList<>();
        String connectorType = showKnowledgeBaseConnectorAbilitiesQo.getConnectorType();
        String connectionId = showKnowledgeBaseConnectorAbilitiesQo.getConnectionId();
        // 传入了合法的connector type，则返回connector type的全量能力
        if (StringUtils.isNotEmpty(connectionId) && !KnowledgeSourceEnum.CUSTOM.toString()
            .equalsIgnoreCase(knowledgeSource)) {
            // 1. 获取connection-id对应的能力
            KnowledgeBaseConnectionEntity connection = connectionMapper.findConnectionById(connectionId);
            if (connection == null) {
                log.error("Cannot find info for connection to get the ability.");
                throw new AgentBaseException(ErrorCode.FAIL_TO_GET_ABILITY);
            }

            // 2. 获取connector-type对应的能力
            List<KnowledgeBaseConnectorAbilityEntity> connectorAbilities = connectorAbilityMapper.listConnectorAbility(
                connection.getConnectorId());
            if (CollectionUtils.isEmpty(connectorAbilities)) {
                log.error("Cannot find ability for this connector.");
                throw new AgentBaseException(ErrorCode.FAIL_TO_GET_ABILITY);
            }

            // 3. 如果connection的used_abilities是null，默认返回connector type对应的全部能力；否则，返回connection和connector的能力交集
            if (CollectionUtils.isEmpty(connection.getUsedAbilities())) {
                abilitiesForResult = getAbilityInfos(abilitiesForResult, connectorAbilities);
            } else {
                try {
                    List<AbilityDetail> usedAbilities = connection.getUsedAbilities();
                    Map<String, KnowledgeBaseConnectorAbilityEntity> metaAbilityMap = connectorAbilities.stream()
                        .collect(Collectors.toMap(KnowledgeBaseConnectorAbilityEntity::getCode, e -> e));

                    // 4. 取connection-id对应能力和connector-type对应能力的交集，并且补充相关的信息
                    abilitiesForResult = getCustomizedAbilities(usedAbilities, metaAbilityMap);
                } catch (Exception e) {
                    log.error("Failed to get the intersection of connection and connector abilities.", e);
                    throw new AgentBaseException(ErrorCode.FAIL_TO_GET_ABILITY);
                }
            }

        } else if (KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            List<KnowledgeBaseConnectorAbilityEntity> connectorAbilities = connectorAbilityMapper.listConnectorAbility(
                "default");
            abilitiesForResult = getAbilityInfos(abilitiesForResult, connectorAbilities);
        } else if (StringUtils.isNotEmpty(connectorType)) {
            List<KnowledgeBaseConnectorAbilityEntity> connectorAbilities = connectorAbilityMapper.listConnectorAbility(
                connectorType);
            abilitiesForResult = getAbilityInfos(abilitiesForResult, connectorAbilities);
        }

        // 国际化处理 Abilities 信息
        translateAbilities(abilitiesForResult);

        return new ShowKnowledgeBaseConnectorAbilitiesResponseBody().setAbilities(abilitiesForResult);
    }

    @Override
    public ListThirdPartyKnowledgeBaseConnectionsResponse listThirdPartyKnowledgeBaseConnections(String projectId,
        ListThirdPartyKnowledgeBaseConnectionsQo basesQo) {
        PageHelper.offsetPage(basesQo.getOffset(), basesQo.getLimit());
        String realQueryName = SqlLikeEscapeHelper.escapeLikeCharacters(basesQo.getName());
        List<KnowledgeBaseConnectionEntity> connectionEntities = connectionMapper.listThirdPartyKnowledgeBases(
            realQueryName, basesQo.getConnectorId(), basesQo.getStatus(), basesQo.getWorkspaceId(), projectId);
        PageInfo<KnowledgeBaseConnectionEntity> pageInfo = new PageInfo<>(connectionEntities);
        List<ThirdPartyKnowledgeBaseConnectionListItem> items = connectionEntities.stream()
            .map(this::convertThirdPartyKnowledgeBaseListItem)
            .toList();
        return new ListThirdPartyKnowledgeBaseConnectionsResponse().setItems(items).setTotal(pageInfo.getTotal());
    }

    @Override
    public ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody showThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, String knowledgeBaseConnectionId) {
        KnowledgeBaseConnectionEntity entity = knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(
            workspaceId, knowledgeBaseConnectionId);
        // 清除敏感信息:密码等
        knowledgeBaseConnectionValidateService.cleanSensitiveInformation(entity);
        return new ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody().setKnowledgeBaseConnectionDetail(
            convertKnowledgeBaseEntityToDetail(entity));
    }

    @Override
    public TestConnectionThirdPartyKnowledgeBaseResponseBody testConnectionThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, testConnectionThirdPartyKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        ConnectorDefinition connectorDefinition = convertRequestBodyToDefinition(body);
        // 检查输入的url是否合法
        if (ObjectUtils.isNotEmpty(connectorDefinition)) {
            knowledgeBaseConnectionValidateService.validateParamDefinitionsAddress(
                connectorDefinition.getParamDefinitions());
        }
        try {
            PageResult<ExternalKnowledgeBaseInfo> pageResult = knowledgeBaseConnectorContext.listKnowledgeBase(null, 0,
                1, connectorDefinition, RequestContextUtils.getRequestAuthToken());
            return new TestConnectionThirdPartyKnowledgeBaseResponseBody().setResult(true);
        } catch (Exception e) {
            log.error("Connect to {} failed,error message:{}", body.getConnectorId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_CONNECT_FAILED);
        }
    }

    @Override
    public TestConnectionThirdPartyKnowledgeBaseResponseBody testConnectionThirdPartyKnowledgeBaseConnectionById(
        String projectId, String knowledgeBaseConnectionId, String workspaceId) {
        log.info(
            "operation log projectId: {}, userId:{}, testConnectionThirdPartyKnowledgeBaseConnectionById, connectionId: {}",
            projectId, RequestContextUtils.getRequestUserId(), knowledgeBaseConnectionId);
        ConnectorDefinition connectorDefinition = getConnectionDefinition(knowledgeBaseConnectionId, workspaceId);
        // 检查输入的url是否合法
        knowledgeBaseConnectionValidateService.validateParamDefinitionsAddress(
            connectorDefinition.getParamDefinitions());
        try {
            PageResult<ExternalKnowledgeBaseInfo> pageResult = knowledgeBaseConnectorContext.listKnowledgeBase(null, 0,
                1, connectorDefinition, RequestContextUtils.getRequestAuthToken());
            return new TestConnectionThirdPartyKnowledgeBaseResponseBody().setResult(true);
        } catch (Exception e) {
            log.error("Connect to {} failed,error message:{}", connectorDefinition.getCode(), e.getMessage());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_CONNECT_FAILED);
        }
    }

    /**
     * 根据连接ID找到对应的连接信息
     *
     * @param connectionId
     * @return
     */
    private ConnectorDefinition getConnectionDefinition(String connectionId, String workSpaceId) {
        List<KnowledgeBaseConnection> connections = knowledgeBaseConnectionMapper.batchQueryKnowledgeBaseConnections(
            Lists.newArrayList(connectionId), workSpaceId);
        if (CollectionUtils.isEmpty(connections)) {
            log.warn("can not find third part connection for connectionId:{} in workspace {}", connectionId,
                workSpaceId);
            throw new AgentBaseException(ErrorCode.THIRD_PARTY_KNOWLEDGE_CONNECTION_NOT_EXISTS, connectionId);
        }
        KnowledgeBaseConnectorEntity connector = connectorMapper.find(connections.get(0).getConnectorId());
        ConnectorDefinition connectorDefinition = convertConnectionToDefinition(connections.get(0));
        Map<String, KnowledgeBaseConnectorParam> paramMap = connector.getParams()
            .stream()
            .collect(Collectors.toMap(KnowledgeBaseConnectorParam::getCode, item -> item));
        connectorDefinition.getParamDefinitions().forEach(param -> {
            if (paramMap.containsKey(param.getCode())) {
                param.setType(ConnectionParamType.valueOf(paramMap.get(param.getCode()).getType().toString()));
            }
        });
        return connectorDefinition;
    }

    private ConnectorDefinition convertConnectionToDefinition(KnowledgeBaseConnection connection) {
        if (Objects.isNull(connection)) {
            return null;
        }
        List<ConnectorParamDefinition> params = JacksonUtils.readValue(connection.getParams(),
            new TypeReference<List<ConnectorParamDefinition>>() { });
        return ConnectorDefinition.builder()
            .code(connection.getConnectorId())
            .connectionId(connection.getId())
            .domainId(connection.getDomainId())
            .paramDefinitions(params)
            .build();
    }

    private ConnectorDefinition convertRequestBodyToDefinition(
        TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody body) {
        if (Objects.isNull(body)) {
            return null;
        }
        List<ConnectorParamDefinition> paramList = body.getParams()
            .stream()
            .map(item -> ConnectorParamDefinition.builder()
                .code(item.getCode())
                .value(item.getValue())
                .type(ConnectionParamType.STRING)
                .build())
            .toList();
        return ConnectorDefinition.builder().code(body.getConnectorId()).paramDefinitions(paramList).build();
    }

    @Override
    public UpdateThirdPartyKnowledgeBaseConnectionResponse updateThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, String knowledgeBaseConnectionId, UpdateThirdPartyKnowledgeBaseConnectionRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, updateThirdPartyKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        KnowledgeBaseConnectionEntity oldConnectionEntity
            = knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(workspaceId,
            knowledgeBaseConnectionId);
        knowledgeBaseConnectionValidateService.checkThirdPartyKnowledgeBaseConnectionName(workspaceId, body.getName(),
            knowledgeBaseConnectionId);
        if (body.getParams() != null) {
            knowledgeBaseConnectionValidateService.checkAndEncryptedParams(oldConnectionEntity.getConnectorId(),
                body.getParams());
        }
        KnowledgeBaseConnectionEntity connectionEntity = new KnowledgeBaseConnectionEntity();
        connectionEntity.setId(knowledgeBaseConnectionId);
        connectionEntity.setConnectorId(oldConnectionEntity.getConnectorId());
        connectionEntity.setName(body.getName());
        connectionEntity.setDescription(body.getDescription());
        connectionEntity.setIcon(body.getIcon());
        connectionEntity.setParams(body.getParams() == null
            ? oldConnectionEntity.getParams()
            : body.getParams()
                .stream()
                .map(param -> new KnowledgeBaseConnectionParam(param.getCode(), param.getValue()))
                .toList());
        connectionEntity.setUpdateTime(System.currentTimeMillis());
        connectionEntity.setUpdateUserId(RequestContextUtils.getRequestUserId());
        connectionEntity.setUpdateUserName(RequestContextUtils.getRequestUserName());
        if (StringUtils.isBlank(body.getIcon())) {
            connectionEntity.setIcon(oldConnectionEntity.getIcon());
        } else {
            IconValidator.validate(body.getIcon(), iconMaxSize, iconType);
            connectionEntity.setIcon(body.getIcon());
        }
        // 相同连接信息校验
        if (knowledgeBaseConnectionValidateService.checkConnectionUnique(workspaceId, connectionEntity)) {
            log.error("connection info {} already exist", connectionEntity.getParams());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_CONNECTION_ALREADY_EXIST);
        }
        connectionMapper.update(connectionEntity);
        return new UpdateThirdPartyKnowledgeBaseConnectionResponse().setId(knowledgeBaseConnectionId);
    }

    private KnowledgeBaseConnectorDetail convertConnectorEntityToConnectorItem(
        KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity) {
        KnowledgeBaseConnectorDetail connectorDetail = new KnowledgeBaseConnectorDetail();
        connectorDetail.setId(knowledgeBaseConnectorEntity.getId());
        connectorDetail.setName(knowledgeBaseConnectorEntity.getName());
        connectorDetail.setIcon(knowledgeBaseConnectorEntity.getIcon());
        connectorDetail.setDescription(knowledgeBaseConnectorEntity.getDescription());
        connectorDetail.setDeployMode(knowledgeBaseConnectorEntity.getDeployMode());
        connectorDetail.setHelpText(knowledgeBaseConnectorEntity.getHelpText());
        if (!CollectionUtils.isEmpty(knowledgeBaseConnectorEntity.getParams())) {
            connectorDetail.setParamDefinition(
                convertKnowledgeBaseConnectorParamsToParamDefinitionList(knowledgeBaseConnectorEntity.getParams()));
        }
        connectorDetail.setDomainName(knowledgeBaseConnectorEntity.getDomainName());
        connectorDetail.setWorkspaceId(knowledgeBaseConnectorEntity.getWorkspaceId());
        connectorDetail.setCreateTime(knowledgeBaseConnectorEntity.getCreateTime());
        connectorDetail.setUpdateUserId(knowledgeBaseConnectorEntity.getUpdateUserId());
        connectorDetail.setUpdateTime(knowledgeBaseConnectorEntity.getUpdateTime());
        connectorDetail.setUpdateUserName(knowledgeBaseConnectorEntity.getUpdateUserName());
        return connectorDetail;
    }

    private KnowledgeBaseConnectionEntity toKnowledgeBaseConnectionEntity(String projectId, String workspaceId,
        CreateThirdPartyKnowledgeBaseConnectionRequestBody request) {
        KnowledgeBaseConnectionEntity entity = new KnowledgeBaseConnectionEntity();
        long currentTimeMillis = System.currentTimeMillis();
        entity.setId(UUIDGenerator.getUUID());
        entity.setProjectId(projectId);
        entity.setConnectorId(request.getConnectorId());
        entity.setWorkspaceId(workspaceId);
        entity.setStatus(KnowledgeBaseConnectionStatus.CLOSE);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setParams(request.getParams()
            .stream()
            .map(param -> new KnowledgeBaseConnectionParam(param.getCode(), param.getValue()))
            .toList());
        entity.setProjectId(projectId);
        entity.setDomainId(RequestContextUtils.getRequestUserDomainId());
        entity.setDomainName(RequestContextUtils.getRequestUserDomainName());
        entity.setCreatedUserId(RequestContextUtils.getRequestUserId());
        entity.setCreatedUserName(RequestContextUtils.getRequestUserName());
        entity.setCreateTime(currentTimeMillis);
        entity.setUpdateTime(currentTimeMillis);
        entity.setUpdateUserId(RequestContextUtils.getRequestUserId());
        entity.setUpdateUserName(RequestContextUtils.getRequestUserName());
        return entity;
    }

    private ThirdPartyKnowledgeBaseConnectionDetail convertKnowledgeBaseEntityToDetail(
        KnowledgeBaseConnectionEntity entity) {
        return new ThirdPartyKnowledgeBaseConnectionDetail().setId(entity.getId())
            .setName(entity.getName())
            .setIcon(entity.getIcon())
            .setStatus(ThirdPartyKnowledgeBaseConnectionDetail.StatusEnum.valueOf(entity.getStatus().getValue()))
            .setDescription(entity.getDescription())
            .setParams(entity.getParams()
                .stream()
                .map(param -> new ConnectionParamInfo().setCode(param.getCode()).setValue(param.getValue()))
                .toList())
            .setConnectorId(entity.getConnectorId())
            .setConnectorName(entity.getConnectorName())
            .setWorkspaceId(entity.getWorkspaceId())
            .setDomainName(entity.getDomainName())
            .setCreateUserId(entity.getUpdateUserId())
            .setCreateUserName(entity.getUpdateUserName())
            .setUpdateUserId(entity.getUpdateUserId())
            .setUpdateUserName(entity.getUpdateUserName());
    }

    private ThirdPartyKnowledgeBaseConnectionListItem convertThirdPartyKnowledgeBaseListItem(
        KnowledgeBaseConnectionEntity entity) {
        return new ThirdPartyKnowledgeBaseConnectionListItem().setId(entity.getId())
            .setName(entity.getName())
            .setIcon(entity.getIcon())
            .setStatus(ThirdPartyKnowledgeBaseConnectionListItem.StatusEnum.valueOf(entity.getStatus().name()))
            .setDescription(entity.getDescription())
            .setConnectorId(entity.getConnectorId())
            .setConnectorName(entity.getConnectorName())
            .setWorkspaceId(entity.getWorkspaceId())
            .setDomainName(entity.getDomainName())
            .setCreateUserId(entity.getUpdateUserId())
            .setCreateUserName(entity.getUpdateUserName())
            .setCreateTime(entity.getCreateTime())
            .setUpdateUserId(entity.getUpdateUserId())
            .setUpdateUserName(entity.getUpdateUserName())
            .setUpdateTime(entity.getUpdateTime());
    }

    private List<ParamDefinitionInfo> convertKnowledgeBaseConnectorParamsToParamDefinitionList(
        List<KnowledgeBaseConnectorParam> params) {
        List<ParamDefinitionInfo> paramDefinitionList = new ArrayList<>(params.size());
        for (KnowledgeBaseConnectorParam param : params) {
            ParamDefinitionInfo paramDefinitionInfo = new ParamDefinitionInfo().setName(param.getName())
                .setCode(param.getCode())
                .setNeed(param.getNeed())
                .setType(ParamDefinitionInfo.TypeEnum.valueOf(param.getType().name()))
                .setRemark(param.getRemark())
                .setPattern(param.getPattern());
            paramDefinitionList.add(paramDefinitionInfo);
        }
        return paramDefinitionList;
    }

    /**
     * @return userLocale 用户语言类型
     */
    private String getUserLocale() {
        String userLocale = I18nUtils.getLocaleInContext().toLanguageTag().toLowerCase(Locale.ROOT);
        if (!i18nTransService.containsLocale(userLocale)) {
            log.error("Unsupported locale: [{}], return default: zh-cn", userLocale);

            // 默认返回 zh-cn 兜底
            return Locale.CHINESE.toLanguageTag().toLowerCase(Locale.ROOT);
        }
        return userLocale;
    }

    /**
     * 翻译连接器列表响应
     *
     * @param connectors 连接器详情列表
     */
    private void translateConnectors(List<KnowledgeBaseConnectorDetail> connectors) {
        if (CollectionUtils.isEmpty(connectors)) {
            return;
        }

        String userLocale = getUserLocale();
        log.info("Start translating connectors, user language: [{}]", userLocale);

        for (KnowledgeBaseConnectorDetail conn : connectors) {
            String connectorId = conn.getId(); // e.g., LakeSearch

            // 1. 替换 Description
            String descKey = String.format("connector.%s.description", connectorId);
            String translatedDesc = i18nTransService.getMessage(userLocale, descKey, conn.getDescription());
            conn.setDescription(translatedDesc);

            // 2. 替换 param_definition 列表
            if (!CollectionUtils.isEmpty(conn.getParamDefinition())) {
                for (ParamDefinitionInfo param : conn.getParamDefinition()) {
                    String paramCode = param.getCode(); // e.g., endpoint

                    // 替换 Name
                    String nameKey = String.format("connector.%s.param.%s.name", connectorId, paramCode);
                    String translatedName = i18nTransService.getMessage(userLocale, nameKey, param.getName());
                    param.setName(translatedName);

                    // 替换 Remark
                    String remarkKey = String.format("connector.%s.param.%s.remark", connectorId, paramCode);
                    String translatedRemark = i18nTransService.getMessage(userLocale, remarkKey, param.getRemark());
                    param.setRemark(translatedRemark);
                }
            }

            log.info("Translated connector [{}] config, user language: [{}]", connectorId, userLocale);
        }
    }

    /**
     * 翻译能力列表响应
     * 处理 sub_ability 字段的 JSON 国际化
     *
     * @param abilities 能力信息列表
     */
    private void translateAbilities(List<AbilityInfo> abilities) {
        if (CollectionUtils.isEmpty(abilities)) {
            return;
        }

        String userLocale = getUserLocale();
        log.info("Start translating abilities, user language: [{}]", userLocale);

        for (AbilityInfo ability : abilities) {
            String abilityCode = ability.getCode(); // e.g., RETRIEVE
            String jsonStr = ability.getSubAbility(); // JSON 字符串

            if (StringUtils.isNotBlank(jsonStr)) {
                try {
                    // 解析 JSON 字符串
                    ObjectNode root = (ObjectNode) objectMapper.readTree(jsonStr);
                    JsonNode modesNode = root.get("search_mode_list");

                    if (modesNode != null && modesNode.isArray()) {
                        ArrayNode searchModeList = (ArrayNode) modesNode;

                        for (JsonNode modeNode : searchModeList) {
                            if (modeNode.isObject()) {
                                ObjectNode modeObj = (ObjectNode) modeNode;
                                String searchMode = modeObj.has("search_mode")
                                    ? modeObj.get("search_mode").asText()
                                    : "";

                                // 动态替换 JSON 内部字段
                                replaceJsonField(modeObj, userLocale, "search_mode_name",
                                    String.format("ability.%s.search_mode.%s.name", abilityCode, searchMode));

                                replaceJsonField(modeObj, userLocale, "summary",
                                    String.format("ability.%s.search_mode.%s.summary", abilityCode, searchMode));

                                replaceJsonField(modeObj, userLocale, "description",
                                    String.format("ability.%s.search_mode.%s.description", abilityCode, searchMode));
                            }
                        }
                    }

                    // 将修改后的 JSON 对象重新转回字符串
                    ability.setSubAbility(root.toString());
                    log.info("Translated ability [{}] configuration, user language: [{}]", abilityCode, userLocale);

                } catch (Exception e) {
                    // 解析失败记录日志，但不阻断流程，返回原始内容
                    log.error("Failed to parse ability [{}] sub_ability JSON, returning original content: {}",
                        abilityCode, e);
                }
            }
        }
    }

    /**
     * 替换 JSON 字段
     *
     * @param node JSON 节点
     * @param locale 语言
     * @param fieldName 字段名
     * @param key 国际化Key
     */
    private void replaceJsonField(ObjectNode node, String locale, String fieldName, String key) {
        if (node.has(fieldName)) {
            String originalVal = node.get(fieldName).asText();
            String translatedVal = i18nTransService.getMessage(locale, key, originalVal);

            if (!originalVal.equals(translatedVal)) {
                node.put(fieldName, translatedVal);
                log.debug("JSON field [{}] translated: [{}] -> [{}]", fieldName, originalVal, translatedVal);
            }
        }
    }

    /**
     * @param entity 知识库实体对象 (KnowledgeBaseConnectorAbilityEntity)
     * @return 基础字段填充完毕的响应对象 (AbilityInfo)
     */
    private AbilityInfo mapAbilityInfoBaseFields(KnowledgeBaseConnectorAbilityEntity entity) {
        return new AbilityInfo().setId(entity.getId())
            .setCode(entity.getCode())
            .setName(entity.getName())
            .setDescription(entity.getDescription())
            .setConnectorId(entity.getConnectorId())
            .setSubAbility(entity.getSubAbility());
    }

    /**
     * 将用户自定义的能力配置与系统元数据进行交集过滤与合并。
     *
     * @param usedAbilities
     * @param metaAbilityMap
     * @return
     */
    private List<AbilityInfo> getCustomizedAbilities(List<AbilityDetail> usedAbilities,
        Map<String, KnowledgeBaseConnectorAbilityEntity> metaAbilityMap) {
        return usedAbilities.stream().map(item -> {
            String ability = item.getAbilityCode();
            boolean abilityEnable = item.isEnable();
            String subAbility = item.getSubAbility();
            KnowledgeBaseConnectorAbilityEntity metaAbilityDetail = metaAbilityMap.get(ability);

            if (metaAbilityDetail == null || !abilityEnable) {
                return null;
            }

            AbilityInfo info = mapAbilityInfoBaseFields(metaAbilityDetail);

            if (metaAbilityDetail.getSubAbility() != null) {
                AbilityProcessor processor = abilityProcessorFactory.getProcessor(ability);
                String enrichedSubAbility = processor.processSubAbility(subAbility, metaAbilityDetail.getSubAbility());
                info.setSubAbility(enrichedSubAbility);
            } else {
                info.setSubAbility(null);
            }
            return info;
        }).filter(Objects::nonNull).toList();
    }

    private List<AbilityInfo> getAbilityInfos(List<AbilityInfo> abilitiesForResult,
        List<KnowledgeBaseConnectorAbilityEntity> connectorAbilities) {
        if (!CollectionUtils.isEmpty(connectorAbilities)) {
            abilitiesForResult = connectorAbilities.stream().filter(abilityEntity -> {
                boolean valid = AbilityTypeEnum.isValid(AbilityTypeEnum.valueOf(abilityEntity.getCode()));
                if (!valid) {
                    log.warn("Ability code {} from DB is not defined in AbilityTypeEnum, skipping.",
                        abilityEntity.getCode());
                }
                return valid;
            }).map(this::mapAbilityInfoBaseFields).toList();
        }
        return abilitiesForResult;
    }
}
