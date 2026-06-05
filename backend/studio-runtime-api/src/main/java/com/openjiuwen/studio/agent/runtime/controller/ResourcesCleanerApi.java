/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.ResourceClearResp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "ResourcesCleaner", description = "the ResourcesCleaner API")
@Validated

/**
 * ResourcesCleanerApi interface
 */
public interface ResourcesCleanerApi {
    @ApiOperation(value = "清理某个资源关联的各种缓存", nickname = "clearResource",
        notes = "清理某个资源关联的各种缓存", response = ResourceClearResp.class, tags = {"ResourcesCleaner"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "清空用户画像记忆的值的响应体", response = ResourceClearResp.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = ErrorRsp.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-runtime/resource/{resource_id}/clear",
        produces = {"application/json"}, method = RequestMethod.DELETE)
    ResponseEntity<ResourceClearResp> clearResource(@Size(min = 1, max = 64)
    @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
    @PathVariable("project_id") String projectId, @Size(min = 1, max = 64)
    @Parameter(in = ParameterIn.PATH, description = "资源id", required = true, schema = @Schema())
    @PathVariable("resource_id") String resourceId, @NotNull
    @ApiParam(value = "应用类型，agent-单智能体、controller-多智能体、workflow-工作流", required = true,
        allowableValues = "agent, workflow, controller") @RequestParam(value = "type", required = true) String type);

}
