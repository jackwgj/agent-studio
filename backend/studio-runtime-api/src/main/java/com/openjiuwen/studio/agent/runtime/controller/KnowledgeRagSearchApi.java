/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.runtime.dto.CreateKnowledgeRouterResponse;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseReq;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteStudioKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.SyncKnowledgeReq;
import com.openjiuwen.studio.agent.runtime.dto.UpdateKnowledgeConnectionResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(value = "KnowledgeRagSearch", description = "the KnowledgeRagSearch API")
@Validated
public interface KnowledgeRagSearchApi {

    @ApiOperation(value = "知识库检索", nickname = "searchRagKnowledge", notes = "知识库检索",
        response = RetrievalKnowledgeBasesResponseBody.class, tags = {"KnowledgeRagSearch"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "rag 检索内容", response = RetrievalKnowledgeBasesResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v2/{project_id}/knowledge/rag-search", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<RetrievalKnowledgeBasesResponseBody> searchRagKnowledge(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @ApiParam(value = "检索内容") @Valid @RequestBody(required = false) RetrievalKnowledgeBasesRequestBody body);

    @Operation(summary = "创建知识库", operationId = "createStudioKnowledgeBase", description = "创建知识库", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = CreateStudioKnowledgeResponseBody.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/bases", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
    ResponseEntity<CreateStudioKnowledgeResponseBody> createStudioKnowledgeBase(@NotNull
    @Parameter(in = ParameterIn.DEFAULT, description = "检索内容", required = true, schema = @Schema()) @Valid @RequestBody
                                                                                @Size(min = 1, max = 64) List<CreateStudioKnowledgeBaseReq> body);

    @Operation(summary = "创建知识库连接", operationId = "createStudioKnowledgeBaseConnection", description = "创建知识库连接", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = CreateKnowledgeRouterResponse.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/router", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
    ResponseEntity<CreateKnowledgeRouterResponse> createStudioKnowledgeBaseConnection(@NotNull @Parameter(in = ParameterIn.DEFAULT, description = "检索内容", required = true, schema = @Schema()) @Valid @RequestBody
    CreateStudioKnowledgeBaseConnectionRequestBody body);

    @Operation(summary = "删除知识库", operationId = "deleteStudioKnowledgeBase", description = "删除知识库", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = DeleteStudioKnowledgeBaseResponseBody.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/bases/{base_id}", produces = { "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<DeleteStudioKnowledgeBaseResponseBody> deleteStudioKnowledgeBase(@Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "知识库id。", required = true, schema = @Schema()) @PathVariable("base_id") String baseId);

    @Operation(summary = "根据AccessKey下载faq文档", operationId = "downloadStudioKnowledgeFaqFile", description = "根据AccessKey下载faq文档。", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = Resource.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/bases/{base_id}/faq-files/{file_id}/content", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Resource> downloadStudioKnowledgeFaqFile(@Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "知识库id。", required = true, schema = @Schema()) @PathVariable("base_id") String baseId, @Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "知识文档id。", required = true, schema = @Schema()) @PathVariable("file_id") String fileId);

    @Operation(summary = "根据临时文件id下载文档", operationId = "downloadStudioKnowledgeFile", description = "根据临时文件id下载文档。", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = Resource.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/bases/{base_id}/files/{file_id}/content", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Resource> downloadStudioKnowledgeFile(@Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "知识库id。", required = true, schema = @Schema()) @PathVariable("base_id") String baseId, @Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "文档id。", required = true, schema = @Schema()) @PathVariable("file_id") String fileId);

    @Operation(summary = "通过图片id（accessKey）下载图片", operationId = "downloadStudioKnowledgeImage", description = "通过图片id（accessKey）下载图片。", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = Resource.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/bases-images/{image_id}", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Resource> downloadStudioKnowledgeImage(@Size(min = 1, max = 100) @Parameter(in = ParameterIn.PATH, description = "图片id（accessKey）。", required = true, schema = @Schema()) @PathVariable("image_id") String imageId);
    
    @Operation(summary = "同步知识库信息", operationId = "syncteKnowledgeConnection", description = "同步知识库信息", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/sync/connections", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> syncteKnowledgeConnection(@Parameter(in = ParameterIn.DEFAULT, description = "同步知识库连接请求体", schema = @Schema()) @Valid @RequestBody(required = false) @Size(min = 1, max = 64) List<SyncKnowledgeReq> body);

    @Operation(summary = "编辑第三方链接", operationId = "updateStudioKnowledgeConnection", description = "编辑第三方链接", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(schema = @Schema(implementation = UpdateKnowledgeConnectionResponse.class))), tags = { "StudioKnowledgeBase" })
    @RequestMapping(value = "/v1/studio/knowledge/connections/{connection_id}", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.PUT)
    ResponseEntity<UpdateKnowledgeConnectionResponse> updateStudioKnowledgeConnection(@Size(min = 1, max = 64) @Parameter(in = ParameterIn.PATH, description = "第三方知识库链接id。", required = true, schema = @Schema()) @PathVariable("connection_id") String connectionId, @Parameter(in = ParameterIn.DEFAULT, description = "第三方链接", schema = @Schema()) @Valid @RequestBody(required = false) @Size(min = 1, max = 64) List<ConnectionParamInfo> body);


}
