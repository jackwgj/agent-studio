/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTableColumnsRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTablesRsp;
import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDatasourceQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTableColumnsQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTablesQo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "DatasourceManagement", description = "the DatasourceManagement API")
@Validated

/**
 * DatasourceManagementApi interface
 */ public interface DatasourceManagementApi {
    @ApiOperation(value = "批量删除数据源", nickname = "batchDeleteDatasource", notes = "批量删除数据源，不做解绑",
        response = DatasourceBatchDeleteRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "删除数据源响应体", response = DatasourceBatchDeleteRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/batch-delete", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<DatasourceBatchDeleteRsp> batchDeleteDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$") @Size(min = 1, max = 64)
        @ApiParam(value = "项目空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId, @NotNull @ApiParam(value = "数据源信息请求体", required = true) @Valid @RequestBody
        DatasourceBatchDeleteReq body);

    @ApiOperation(value = "创建一个数据源", nickname = "createDatasource", notes = "创建一个数据源",
        response = DatasourceBriefRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "数据源模型响应体", response = DatasourceBriefRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<DatasourceBriefRsp> createDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$") @Size(min = 1, max = 64)
        @ApiParam(value = "项目空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "数据源信息请求体", required = true) @Valid @RequestBody DatasourceInfoReq body);

    @ApiOperation(value = "删除一个数据源", nickname = "deleteDatasource", notes = "删除一个数据源，不做解绑",
        response = DatasourceDeleteRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "删除数据源响应体", response = DatasourceDeleteRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/{datasource_id}",
        produces = {"application/json"}, method = RequestMethod.DELETE)
    ResponseEntity<DatasourceDeleteRsp> deleteDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "数据源id", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId,
        @NotNull @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$") @Size(min = 1, max = 64)
        @ApiParam(value = "项目空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId);

    @ApiOperation(value = "获取数据源列表", nickname = "listDatasource", notes = "获取数据源列表",
        response = DatasourceListRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "工具", response = DatasourceListRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<DatasourceListRsp> listDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @ApiParam(value = "ListDatasourceQo: converted from multi query params") @Valid
        ListDatasourceQo listDatasourceQo);

    @ApiOperation(value = "修改一个数据源", nickname = "modifyDatasource", notes = "修改一个数据源",
        response = DatasourceBriefRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "修改数据源响应体", response = DatasourceBriefRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/{datasource_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.PUT)
    ResponseEntity<DatasourceBriefRsp> modifyDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Size(max = 64) @Parameter(in = ParameterIn.PATH, description = "数据源id", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId,
        @NotNull @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$") @Size(min = 1, max = 64)
        @ApiParam(value = "项目空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "修改数据源请求体", required = true) @Valid @RequestBody DatasourceInfoReq body);

    @ApiOperation(value = "获取一个数据源", nickname = "retrieveDatasource", notes = "获取一个数据源",
        response = DatasourceInfoRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "模型系信息", response = DatasourceInfoRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/{datasource_id}",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<DatasourceInfoRsp> retrieveDatasource(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "数据源id", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId,
        @NotNull @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$") @Size(min = 1, max = 64)
        @ApiParam(value = "项目空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId);

    @ApiOperation(value = "查询数据源表格字段", nickname = "retrieveDatasourceTableColumns",
        notes = "查询数据源表格字段", response = DatasourceTableColumnsRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "获取数据源表格字段响应体", response = DatasourceTableColumnsRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/{datasource_id}/tables/{table_name}/columns",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<DatasourceTableColumnsRsp> retrieveDatasourceTableColumns(
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Size(max = 64) @Parameter(in = ParameterIn.PATH, description = "数据源id", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId, @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "查询的表格名称", required = true, schema = @Schema())
        @PathVariable("table_name") String tableName,
        @ApiParam(value = "RetrieveDatasourceTableColumnsQo: converted from multi query params") @Valid
        RetrieveDatasourceTableColumnsQo retrieveDatasourceTableColumnsQo);

    @ApiOperation(value = "查询数据源表格", nickname = "retrieveDatasourceTables", notes = "查询数据源表格",
        response = DatasourceTablesRsp.class, tags = {"DatasourceManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "获取数据源表格列表响应体", response = DatasourceTablesRsp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/datasource/{datasource_id}/tables",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<DatasourceTablesRsp> retrieveDatasourceTables(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Size(max = 64) @Parameter(in = ParameterIn.PATH, description = "数据源id", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId,
        @ApiParam(value = "RetrieveDatasourceTablesQo: converted from multi query params") @Valid
        RetrieveDatasourceTablesQo retrieveDatasourceTablesQo);

}
