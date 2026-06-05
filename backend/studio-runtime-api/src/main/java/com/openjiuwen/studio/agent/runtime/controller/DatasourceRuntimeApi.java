/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteResp;

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

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "DatasourceRuntime", description = "the DatasourceRuntime API")
@Validated

/**
 * DatasourceRuntimeApi interface
 */
public interface DatasourceRuntimeApi {
    @ApiOperation(value = "数据源运行接口", nickname = "executeDatasource", notes = "",
        response = DatasourceExecuteResp.class, tags = {"DatasourceRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = DatasourceExecuteResp.class),
        @ApiResponse(code = 400, message = "", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/datasource/{datasource_id}/execute", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<DatasourceExecuteResp> executeDatasource(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("datasource_id") String datasourceId,
        @NotNull @ApiParam(value = "数据源运行请求结构体", required = true) @Valid @RequestBody
        DatasourceExecuteReq body);

}
