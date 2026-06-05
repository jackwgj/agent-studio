package com.openjiuwen.studio.agent.space.api.controller;


import com.openjiuwen.studio.agent.space.api.vo.request.AgentQueryReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentListResp;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentSummaryInfo;
import com.openjiuwen.studio.agent.space.api.vo.response.WorkspaceInfoResp;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * agent管理相关webapi
 */
@Validated
@RequestMapping("/agentspace/webapi/v1/agent-builder")
public interface AgentManagerApi {

    /**
     * agent上线
     * @param agentId
     */
    @PostMapping(value = "/agent/online/{agentId}")
    BaseResp<Boolean> onlineAgent(@PathVariable(value = "agentId") @Valid @Size(max = 64) @ValidNormalString String agentId);

    /**
     * agent下线
     * @param agentId
     */
    @PostMapping(value = "/agent/offline/{agentId}")
    BaseResp<Boolean> offlineAgent(@PathVariable(value = "agentId") @Valid @Size(max = 64) @ValidNormalString String agentId);

    /**
     * agent删除
     * @param agentId
     */
    @PostMapping(value = "/agent/delete/{agentId}")
    BaseResp<Boolean> deleteAgent(@PathVariable(value = "agentId") @Valid @Size(max = 64) @ValidNormalString String agentId);

    /**
     * 添加agent
     * @param agentIds
     */
    @PostMapping(value = "/agent/addBatch")
    BaseResp<Boolean> addBatchAgent(@RequestBody @Valid @Size(min = 1, max = 100) List<String> agentIds);

    /**
     * 查询空间列表
     */
    @GetMapping(value = "/workspace/list")
    BaseResp<WorkspaceInfoResp> getWorkspaceList();

    /**
     * 查询agentStudio所有已发布到agentSpace的agent
     */
    @PostMapping(value = "/agent/query")
    BaseResp<AgentListResp> queryAgentList(@Valid @RequestBody AgentQueryReq req);

    /**
     * 查询agentStudio所有已发布的agent
     */
    @PostMapping(value = "/agent/all")
    BaseResp<AgentListResp> queryAllAgent(@Valid @RequestBody AgentQueryReq req);

    /**
     * 查询agent详情
     */
    @GetMapping(value = "/agent/info/{agentId}")
    BaseResp<AgentSummaryInfo> getAgentInfo(@PathVariable(value = "agentId") @Valid @Size(max = 64) @ValidNormalString String agentId);

}
