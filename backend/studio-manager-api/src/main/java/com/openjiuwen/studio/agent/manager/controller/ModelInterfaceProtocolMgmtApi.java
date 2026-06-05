/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryMdInterfaceProtocolQo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "ModelInterfaceProtocolMgmt", description = "the ModelInterfaceProtocolMgmt API")
@Validated

/**
 * ModelInterfaceProtocolMgmtApi interface
 */ public interface ModelInterfaceProtocolMgmtApi {
    @ApiOperation(value = "", nickname = "queryMdInterfaceProtocol", notes = "查询模型接口协议",
        response = ModelInterfaceProtocolListRsp.class, tags = {"ModelInterfaceProtocolMgmt"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "模型路由策略详情", response = ModelInterfaceProtocolListRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/model-manager/interface-protocol", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ModelInterfaceProtocolListRsp> queryMdInterfaceProtocol(@Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
    @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema()) @PathVariable("project_id")
    String projectId, @ApiParam(value = "QueryMdInterfaceProtocolQo: converted from multi query params") @Valid
    QueryMdInterfaceProtocolQo queryMdInterfaceProtocolQo);

}
