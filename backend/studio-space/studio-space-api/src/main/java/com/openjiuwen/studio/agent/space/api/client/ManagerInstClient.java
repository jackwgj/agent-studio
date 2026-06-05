package com.openjiuwen.studio.agent.space.api.client;

import com.openjiuwen.studio.agent.space.api.vo.request.AgentStudioQueryReq;
import com.openjiuwen.studio.agent.space.api.vo.response.QueryAgentApplicationListRsp;
import com.openjiuwen.studio.agent.space.api.vo.response.WorkspaceInfoResp;
import com.openjiuwen.studio.agent.space.common.feignclient.FeignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@FeignClient(name = "ManagerInstClient", url = "${agent.manager.apig.endpoint}", configuration = {FeignConfig.class})
public interface ManagerInstClient {
    @GetMapping(value = "/v1/{project-id}/agent-manager/applications?status=published")
    ResponseEntity<QueryAgentApplicationListRsp> queryAgentList(@PathVariable("project-id") String projectId,
        @RequestParam("offset") int offset, @RequestParam("limit") int limit, @RequestParam("type") String type,
        @RequestParam(value = "name", required = false) String name,
        @RequestHeader(value = "x-auth-token") String token);

    @PostMapping(value = "/v1/{project-id}/agent-manager/applications")
    ResponseEntity<QueryAgentApplicationListRsp> queryAgentList(@PathVariable("project-id") String projectId,
        @RequestHeader(value = "x-auth-token") String token, @RequestBody AgentStudioQueryReq req);

    @GetMapping(value = "/v1/{project-id}/agent-manager/workspace")
    ResponseEntity<WorkspaceInfoResp> getWorkspaceList(@PathVariable("project-id") String projectId,
        @RequestHeader(value = "x-auth-token") String token);
}
