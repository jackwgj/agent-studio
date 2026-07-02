/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.enums.OperationType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.StrUtils;
import com.openjiuwen.studio.agent.common.utils.ThreadLocalUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceColumnRsp;
import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;
import com.openjiuwen.studio.agent.manager.dto.DatasourceDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTableColumnsRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTablesRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDatasourceQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTableColumnsQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTablesQo;
import com.openjiuwen.studio.agent.manager.entity.DatasourceEntity;
import com.openjiuwen.studio.agent.manager.mapper.DatasourceMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceExecuteRsp;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceStatusCheckRsp;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * DatasourceManagementService实现类
 *
 */
@Slf4j
@Service
public class DatasourceManagementService implements IDatasourceManagementService {
    /**
     * 数据源id
     */
    private static final String ID = "id";

    /**
     * 数据源名称
     */
    private static final String NAME = "name";

    /**
     * 数据源project id
     */
    private static final String PROJECT_ID = "project_id";

    /**
     * 数据源类型
     */
    private static final String TYPE = "type";

    /**
     * 网络接入方式
     */
    private static final String INTERNET_ACCESS = "internet_access";

    /**
     * 连接方式
     */
    private static final String CONNECTION_INFO = "connection_info";

    /**
     * 修改时间
     */
    private static final String UPDATED_ON = "updated_on";

    /**
     * 数据库表名称key
     */
    private static final String TABLE_NAME = "TABLE_NAME";

    /**
     * 数据源查询表格语句
     */
    private static final String QUERY_TABLE = "SELECT table_name, COUNT(*) OVER() AS total_count FROM information_schema.tables WHERE table_schema = '%s' %s ORDER BY `table_name` LIMIT %s OFFSET %s";

    /**
     * 数据源查询表格模糊查询条件
     */
    private static final String FUZZY_QUERY = "AND `table_name` LIKE ?";

    /**
     * 数据源查询表格列字段语句
     */
    private static final String QUERY_COLUMN = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";

    /**
     * 数据库表字段类型key
     */
    private static final String DATA_TYPE = "DATA_TYPE";

    /**
     * 数据库列字段key
     */
    private static final String COLUMN_NAME = "COLUMN_NAME";

    /**
     * 数据库字段描述key
     */
    private static final String COLUMN_COMMENT = "COLUMN_COMMENT";

    /**
     * 数据库表格列表计数key
     */
    private static final String TOTAL_COUNT = "total_count";

    @Autowired
    MgObsService obsService;

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private DatasourceStatusCheckService datasourceStatusCheckService;

    @Value("${datasource.ir-path}")
    private String datasourceIrPath;

    @Value("${datasource.column-template:}")
    private String columnTemplate;

    @Value("${tool.url.enable-check}")
    private boolean enableUrlCheck;

    @Value("${workflow.connector_config_host_blacklist}")
    private String connectorConfigHostBlacklist;

    private static final String COLUMN_TEMPLATE = "{\"bigint\":\"Number\",\"char\":\"String\",\"date\":\"Time\",\"datetime\":\"Time\",\"decimal\":\"Number\",\"double\":\"Number\",\"float\":\"Number\",\"int\":\"Integer\",\"integer\":\"Integer\",\"json\":\"String\",\"longtext\":\"String\",\"numeric\":\"Number\",\"text\":\"String\",\"time\":\"Time\",\"timestamp\":\"Time\",\"tinytext\":\"String\",\"varchar\":\"String\"}";

    @Override
    @OperationLog(
        operationType = OperationType.CREATE,
        resourceType = "Datasource",
        description = "创建数据源",
        resourceId = "-1",
        resourceName = "body.name"
    )
    public DatasourceBriefRsp createDatasource(String projectId, String workspaceId, DatasourceInfoReq body) {
        DatasourceEntity datasource = DatasourceEntity.builder()
            .id(UUID.randomUUID().toString())
            .projectId(projectId)
            .workspaceId(workspaceId)
            .build();

        // 数据源名称重名检查
        duplicateDatasourceNameCheck(projectId, body.getName(), datasource.getId());

        CommonUtil.checkHostValid(enableUrlCheck, body.getConnectionInfo().getHost(), connectorConfigHostBlacklist);

        updateDatasourceEntity(datasource, body);

        Date currentData = new Date();
        datasource.setCreatorId(RequestContextUtils.getRequestUserId());
        datasource.setCreatedBy(RequestContextUtils.getRequestUserName());
        datasource.setUpdaterId(RequestContextUtils.getRequestUserId());
        datasource.setUpdatedBy(RequestContextUtils.getRequestUserName());
        datasource.setDomainId(RequestContextUtils.getRequestUserDomainId());
        datasource.setCreatedOn(currentData);
        datasource.setUpdatedOn(currentData);

        // 上传json文件到Obs
        uploadObs(datasource);

        // 数据库状态检查
        DatasourceStatusCheckRsp datasourceStatusCheckRsp =
            datasourceStatusCheckService.datasourceStatusCheck(datasource.getId());
        datasource.setStatus(datasourceStatusCheckRsp.getStatus());
        datasource.setLastErrorMessage(datasourceStatusCheckRsp.getErrorMessage());

        // 创建数据源
        datasourceMapper.createDatasourceEntity(datasource);
        return convertDatasourceBriefRsp(datasource);
    }

    @Override
    @OperationLog(
        operationType = OperationType.DELETE,
        resourceType = "Datasource",
        description = "删除数据源",
        resourceId = "datasourceId",
        resourceName = ""
    )
    public DatasourceDeleteRsp deleteDatasource(String projectId, String datasourceId, String workspaceId) {
        DatasourceBatchDeleteRsp batchDeleteRsp = batchDeleteDatasource(projectId, workspaceId,
            new DatasourceBatchDeleteReq().setIds(Collections.singletonList(datasourceId)));
        return batchDeleteRsp.getCount() > 0 ? new DatasourceDeleteRsp().setId(batchDeleteRsp.getIds().get(0))
            : new DatasourceDeleteRsp();
    }

    @Override
    public DatasourceListRsp listDatasource(String projectId, ListDatasourceQo listDatasourceQo) {
        PageHelper.offsetPage(listDatasourceQo.getOffset(), listDatasourceQo.getLimit());
        DatasourceEntity search = DatasourceEntity.builder()
            .projectId(projectId)
            .workspaceId(listDatasourceQo.getWorkspaceId())
            .name(StrUtils.filterSpecialWords(listDatasourceQo.getName()))
            .type(listDatasourceQo.getType())
            .internetAccess(listDatasourceQo.getInternetAccess())
            .status(listDatasourceQo.getStatus())
            .createdBy(listDatasourceQo.getCreator())
            .updatedBy(listDatasourceQo.getUpdater())
            .build();
        List<DatasourceEntity> datasourceEntities =
            datasourceMapper.getDatasourceEntity(search, listDatasourceQo.getSort());

        PageInfo<DatasourceEntity> pageInfo = new PageInfo<>(datasourceEntities);

        List<DatasourceBriefRsp> infos = new ArrayList<>();
        datasourceEntities.forEach(datasourceEntity -> infos.add(convertDatasourceBriefRsp(datasourceEntity)));
        return new DatasourceListRsp().setCount(pageInfo.getTotal()).setDatasourceList(infos);
    }

    @Override
    @OperationLog(
        operationType = OperationType.DELETE,
        resourceType = "Datasource",
        description = "批量删除数据源",
        resourceId = "body.ids[0]",
        resourceName = ""
    )
    public DatasourceBatchDeleteRsp batchDeleteDatasource(String projectId, String workspaceId,
        DatasourceBatchDeleteReq body) {
        checkOperatorPermission(body.getIds(), projectId, workspaceId);
        DatasourceBatchDeleteRsp datasourceBatchDeleteRsp = new DatasourceBatchDeleteRsp();
        List<String> items = new ArrayList<>();
        try {
            // 同步删除数据库和obs数据
            datasourceMapper.deleteByIds(body.getIds(), projectId);
            body.getIds().forEach(datasourceId -> {
                obsService.deleteObject(getDatasourceObsPath(datasourceId));
                items.add(datasourceId);
            });
        } catch (Exception e) {
            log.error("delete datasource failed", e);
        }
        datasourceBatchDeleteRsp.setIds(items);
        datasourceBatchDeleteRsp.setCount((long) items.size());
        return datasourceBatchDeleteRsp;
    }

    @Override
    @OperationLog(
        operationType = OperationType.UPDATE,
        resourceType = "Datasource",
        description = "修改数据源",
        resourceId = "datasourceId",
        resourceName = "body.name"
    )
    public DatasourceBriefRsp modifyDatasource(String projectId, String datasourceId, String workspaceId,
        DatasourceInfoReq body) {
        // 创建者有权限修改删除
        checkOperatorPermission(Collections.singletonList(datasourceId), projectId, workspaceId);

        // 数据源名称重名检查
        duplicateDatasourceNameCheck(projectId, body.getName(), datasourceId);

        CommonUtil.checkHostValid(enableUrlCheck, body.getConnectionInfo().getHost(), connectorConfigHostBlacklist);

        DatasourceEntity datasourceEntity =
            DatasourceEntity.builder().id(datasourceId).projectId(projectId).workspaceId(workspaceId).build();
        updateDatasourceEntity(datasourceEntity, body);

        datasourceEntity.setUpdaterId(RequestContextUtils.getRequestUserId());
        datasourceEntity.setUpdatedBy(RequestContextUtils.getRequestUserName());
        datasourceEntity.setUpdatedOn(new Date());

        // 上传json文件到Obs
        uploadObs(datasourceEntity);

        // 数据库状态检查
        DatasourceStatusCheckRsp datasourceStatusCheckRsp =
            datasourceStatusCheckService.datasourceStatusCheck(datasourceId);
        datasourceEntity.setStatus(datasourceStatusCheckRsp.getStatus());
        datasourceEntity.setLastErrorMessage(datasourceStatusCheckRsp.getErrorMessage());

        // 创建数据源
        datasourceMapper.updateByPrimaryKey(datasourceEntity);
        return convertDatasourceBriefRsp(datasourceEntity);
    }

    @Override
    public DatasourceInfoRsp retrieveDatasource(String projectId, String datasourceId, String workspaceId) {
        DatasourceEntity datasourceEntity =
            datasourceMapper.getByPrimaryKeyAndWorkspaceId(datasourceId, projectId, workspaceId);
        if (datasourceEntity == null) {
            return new DatasourceInfoRsp();
        }
        // 对密码信息进行加密
        processCriticalInfo(datasourceEntity, true);
        return convertDatasourceInfoRsp(datasourceEntity);
    }

    @Override
    public DatasourceTableColumnsRsp retrieveDatasourceTableColumns(String projectId, String datasourceId, String tableName, RetrieveDatasourceTableColumnsQo retrieveDatasourceTableColumnsQo) {
        checkOperatorPermission(Collections.singletonList(datasourceId), projectId, retrieveDatasourceTableColumnsQo.getWorkspaceId());
        DatasourceEntity datasourceEntity = datasourceMapper.getByPrimaryKeyAndWorkspaceId(datasourceId, projectId, retrieveDatasourceTableColumnsQo.getWorkspaceId());
        DatasourceConnectionInfo connectionInfo =
                JsonUtils.json2ObjQuietly(datasourceEntity.getConnectionInfo(), DatasourceConnectionInfo.class);
        String querySql = String.format(QUERY_COLUMN, connectionInfo.getDatabaseName(), tableName);
        columnTemplate = StringUtils.isEmpty(columnTemplate) ? COLUMN_TEMPLATE : columnTemplate;
        JSONObject columnMap = JSON.parseObject(columnTemplate);
        DatasourceExecuteRsp datasourceExecuteRsp =
                datasourceStatusCheckService.retrieveDatasourceBySql(datasourceId, querySql, null);
        List<DatasourceColumnRsp> filedList = datasourceExecuteRsp.getOutputList().stream().filter(obj -> columnMap.containsKey(JSONObject.from(obj).get(DATA_TYPE).toString())).map(obj -> {
            DatasourceColumnRsp datasourceColumnRsp = new DatasourceColumnRsp();
            datasourceColumnRsp.setName(JSONObject.from(obj).get(COLUMN_NAME).toString());
            datasourceColumnRsp.setType(columnMap.getString(JSONObject.from(obj).get(DATA_TYPE).toString()));
            datasourceColumnRsp.setDescription(JSONObject.from(obj).get(COLUMN_COMMENT).toString());
            return datasourceColumnRsp;
        }).toList();
        return new DatasourceTableColumnsRsp().setFieldList(filedList);
    }

    @Override
    public DatasourceTablesRsp retrieveDatasourceTables(String projectId, String datasourceId, RetrieveDatasourceTablesQo retrieveDatasourceTablesQo) {
        // 创建者有权限查询表格
        checkOperatorPermission(Collections.singletonList(datasourceId), projectId, retrieveDatasourceTablesQo.getWorkspaceId());
        DatasourceEntity datasourceEntity = datasourceMapper.getByPrimaryKeyAndWorkspaceId(datasourceId, projectId, retrieveDatasourceTablesQo.getWorkspaceId());
        DatasourceConnectionInfo connectionInfo =
                JsonUtils.json2ObjQuietly(datasourceEntity.getConnectionInfo(), DatasourceConnectionInfo.class);
        String querySql;
        List<String> conditions = null;
        if (StringUtils.isEmpty(retrieveDatasourceTablesQo.getTableName())) {
            querySql = String.format(QUERY_TABLE, connectionInfo.getDatabaseName(), "", retrieveDatasourceTablesQo.getLimit(), retrieveDatasourceTablesQo.getOffset());
        } else {
            conditions = Collections.singletonList("%" + retrieveDatasourceTablesQo.getTableName() + "%");
            querySql = String.format(QUERY_TABLE, connectionInfo.getDatabaseName(), FUZZY_QUERY, retrieveDatasourceTablesQo.getLimit(), retrieveDatasourceTablesQo.getOffset());
        }
        DatasourceExecuteRsp datasourceExecuteRsp =
                datasourceStatusCheckService.retrieveDatasourceBySql(datasourceId, querySql, conditions);
        if (CollectionUtils.isEmpty(datasourceExecuteRsp.getOutputList())) {
            return new DatasourceTablesRsp().setCount(datasourceExecuteRsp.getRowNum()).setTables(datasourceExecuteRsp.getOutputList().stream().map(obj -> JSONObject.from(obj).get(TABLE_NAME).toString()).toList());
        }
        return new DatasourceTablesRsp().setCount(Integer.parseInt(JSONObject.from(datasourceExecuteRsp.getOutputList().stream().findFirst()).get(TOTAL_COUNT).toString())).setTables(datasourceExecuteRsp.getOutputList().stream().map(obj -> JSONObject.from(obj).get(TABLE_NAME).toString()).toList());
    }

    private void processCriticalInfo(DatasourceEntity datasourceEntity, boolean shouldAnonymized) {
        DatasourceConnectionInfo datasourceConfig =
            JsonUtils.json2ObjQuietly(datasourceEntity.getConnectionInfo(), DatasourceConnectionInfo.class);
        if (datasourceConfig == null) {
            datasourceEntity.setConnectionInfo(null);
            return;
        }
        if (shouldAnonymized) {
            // 敏感信息匿名化回传给前端
            datasourceConfig.setPassword(CommonConstant.ANONYMIZED_TEXT);
        } else {
            // 前端未修改回传的匿名字符，在数据库找到源信息填入
            if (CommonConstant.ANONYMIZED_TEXT.equals(datasourceConfig.getPassword())) {
                DatasourceEntity origin = datasourceMapper.getByPrimaryKeyAndWorkspaceId(datasourceEntity.getId(),
                    datasourceEntity.getProjectId(), datasourceEntity.getWorkspaceId());

                DatasourceConnectionInfo srcConnectionInfo = null;
                if (!Objects.isNull(origin)) {
                    srcConnectionInfo =
                        JsonUtils.json2ObjQuietly(origin.getConnectionInfo(), DatasourceConnectionInfo.class);
                }
                // 保证user和host相同情况下解析对应参数
                if (srcConnectionInfo == null
                    || !Strings.CS.equals(srcConnectionInfo.getUser(), datasourceConfig.getUser())
                    || !Strings.CS.equals(srcConnectionInfo.getHost(), datasourceConfig.getHost())) {
                    throw new AgentStudioException(StudioError.DATASOURCE_AUTH_FAILED);
                }
                // 原密文转换为kms密文
                datasourceConfig.setPassword(CryptoUtils.encrypt(srcConnectionInfo.getPassword()));
            } else {
                // 前端传入明文需kms加密
                datasourceConfig.setPassword(CryptoUtils.encrypt(datasourceConfig.getPassword()));
            }
        }
        datasourceEntity.setConnectionInfo(JsonUtils.toJson(datasourceConfig));
    }

    private void updateDatasourceEntity(DatasourceEntity datasourceEntity, DatasourceInfoReq datasourceInfoReq) {
        datasourceEntity.setName(datasourceInfoReq.getName());
        datasourceEntity.setDesc(datasourceInfoReq.getDesc());
        datasourceEntity.setType(datasourceInfoReq.getType());
        datasourceEntity.setInternetAccess(datasourceInfoReq.getInternetAccess());
        datasourceEntity.setInstanceId(datasourceInfoReq.getInstanceId());
        datasourceEntity.setInstanceName(datasourceInfoReq.getInstanceName());
        datasourceEntity.setConnectionInfo(JsonUtils.toJson(datasourceInfoReq.getConnectionInfo()));

        // 对明文进行加密，匿名化字符从数据源读取
        processCriticalInfo(datasourceEntity, false);
    }

    private void checkOperatorPermission(List<String> ids, String projectId, String workspaceId) {
        // 创建者有权限修改删除
        List<DatasourceEntity> datasourceEntities = datasourceMapper.getByIds(ids);
        if (datasourceEntities.isEmpty()) {
            throw new AgentStudioException(StudioError.NO_PERMISSION_MODIFY);
        }
        datasourceEntities.forEach(datasourceEntity -> {
            if (!Strings.CS.equals(datasourceEntity.getProjectId(), projectId)
                || !Strings.CS.equals(datasourceEntity.getWorkspaceId(), workspaceId)) {
                throw new AgentStudioException(StudioError.NO_PERMISSION_MODIFY);
            }
        });
    }

    private void uploadObs(DatasourceEntity datasourceEntity) {
        Map<String, Object> info = getDataSourceIrInfo(datasourceEntity);

        // sql存放在bucket/datasource/ir
        obsService.uploadObsFile(getDatasourceObsPath(datasourceEntity.getId()),
            JSON.toJSONString(info, JSONWriter.Feature.WriteMapNullValue), -1);
    }

    private DatasourceInfoRsp convertDatasourceInfoRsp(DatasourceEntity entity) {
        // 数据库信息，转换为api body体
        return new DatasourceInfoRsp().setId(entity.getId())
            .setName(entity.getName())
            .setDescription(entity.getDesc())
            .setType(entity.getType())
            .setInternetAccess(entity.getInternetAccess())
            .setInstanceId(entity.getInstanceId())
            .setInstanceName(entity.getInstanceName())
            .setConnectionInfo(JsonUtils.json2ObjQuietly(entity.getConnectionInfo(), DatasourceConnectionInfo.class))
            .setStatus(entity.getStatus())
            .setErrorMessage(StringUtils.isNotEmpty(entity.getLastErrorMessage()) ? entity.getLastErrorMessage() : null)
            .setCreator(entity.getCreatedBy())
            .setCreatorId(entity.getCreatorId())
            .setCreateTime(entity.getCreatedOn())
            .setUpdater(entity.getUpdatedBy())
            .setUpdaterId(entity.getUpdaterId())
            .setUpdateTime(entity.getUpdatedOn());
    }

    private Map<String, Object> getDataSourceIrInfo(DatasourceEntity datasourceEntity) {
        Map<String, Object> info = new HashMap<>();
        if (Objects.isNull(datasourceEntity)) {
            return info;
        }

        info.put(ID, datasourceEntity.getId());
        info.put(PROJECT_ID, datasourceEntity.getProjectId());
        info.put(NAME, datasourceEntity.getName());
        info.put(TYPE, datasourceEntity.getType());
        info.put(INTERNET_ACCESS, datasourceEntity.getInternetAccess());
        DatasourceConnectionInfo connectionInfo =
            JsonUtils.json2ObjQuietly(datasourceEntity.getConnectionInfo(), DatasourceConnectionInfo.class);
        info.put(CONNECTION_INFO, connectionInfo);
        info.put(UPDATED_ON, datasourceEntity.getUpdatedOn().getTime());

        return info;
    }

    private String getDatasourceObsPath(String datasourceId) {
        return String.format("%s/%s.json", datasourceIrPath, datasourceId);
    }

    private DatasourceBriefRsp convertDatasourceBriefRsp(DatasourceEntity entity) {
        // 数据源信息转换为brief Rsp，供创建修改以及列表接口使用
        return new DatasourceBriefRsp().setId(entity.getId())
            .setName(entity.getName())
            .setDescription(entity.getDesc())
            .setType(entity.getType())
            .setInternetAccess(entity.getInternetAccess())
            .setInstanceId(entity.getInstanceId())
            .setInstanceName(entity.getInstanceName())
            .setStatus(entity.getStatus())
            .setErrorMessage(StringUtils.isNotEmpty(entity.getLastErrorMessage()) ? entity.getLastErrorMessage() : null)
            .setCreator(entity.getCreatedBy())
            .setCreatorId(entity.getCreatorId())
            .setCreateTime(entity.getCreatedOn())
            .setUpdater(entity.getUpdatedBy())
            .setUpdaterId(entity.getUpdaterId())
            .setUpdateTime(entity.getUpdatedOn());
    }

    private void duplicateDatasourceNameCheck(String projectId, String datasourceName, String datasourceId) {
        List<DatasourceEntity> srcDatasource =
            datasourceMapper.getByName(datasourceName, projectId, ThreadLocalUtils.getWorkspaceId());
        if (!srcDatasource.isEmpty() && !Strings.CS.equals(srcDatasource.get(0).getId(), datasourceId)) {
            throw new AgentStudioException(StudioError.DATASOURCE_NAME_REPEATED);
        }
    }
}
