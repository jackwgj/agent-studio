package com.openjiuwen.studio.agent.space.app.builder;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.space.api.controller.AgentManagerApi;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentQueryReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentListResp;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentSummaryInfo;
import com.openjiuwen.studio.agent.space.api.vo.response.WorkspaceInfoResp;
import com.openjiuwen.studio.agent.space.app.service.client.AgentManagerService;
import com.openjiuwen.studio.agent.space.app.service.builder.AgentBuilderService;
import com.openjiuwen.studio.agent.space.app.util.iam.IamRoleUtil;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class AgentManagerApiController implements AgentManagerApi {
    @Resource
    private AgentBuilderService AgentBuilderService;

    @Resource
    private AgentManagerService agentManagerService;

    @Override
    public BaseResp<Boolean> onlineAgent(String agentId) {
        log.info("AgentBuilderApiController.onlineAgent start. agentId: {}", agentId);
        if (!IamRoleUtil.checkAdminRole()) {
            log.error("onlineAgent failed. no permission.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.NO_OPERATION_PERMISSION);
        }
        return BaseResp.success(AgentBuilderService.onlineAgent(agentId));
    }

    @Override
    public BaseResp<Boolean> offlineAgent(String agentId) {
        log.info("AgentBuilderApiController.offlineAgent start. agentId: {}", agentId);
        if (!IamRoleUtil.checkAdminRole()) {
            log.error("offlineAgent failed. no permission.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.NO_OPERATION_PERMISSION);
        }
        return BaseResp.success(AgentBuilderService.offlineAgent(agentId));
    }

    @Override
    public BaseResp<Boolean> deleteAgent(String agentId) {
        log.info("AgentBuilderApiController.deleteAgent start. agentId: {}", agentId);
        if (!IamRoleUtil.checkAdminRole()) {
            log.error("deleteAgent failed. no permission.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.NO_OPERATION_PERMISSION);
        }
        return BaseResp.success(AgentBuilderService.deleteAgent(agentId));
    }

    @Override
    public BaseResp<Boolean> addBatchAgent(List<String> agentIds) {
        log.info("AgentBuilderApiController.addBatchAgent start. agentIds: {}", JSON.toJSONString(agentIds));
        if (!IamRoleUtil.checkAdminRole()) {
            log.error("addBatchAgent failed. no permission.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.NO_OPERATION_PERMISSION);
        }
        return BaseResp.success(AgentBuilderService.addBatchAgent(agentIds));
    }

    @Override
    public BaseResp<WorkspaceInfoResp> getWorkspaceList() {
        log.info("AgentBuilderApiController.getWorkspaceList start.");
        return BaseResp.success(AgentBuilderService.getWorkspaceList());
    }

    @Override
    public BaseResp<AgentListResp> queryAgentList(AgentQueryReq req) {
        log.info("AgentManagerApiController.queryAgentList start. req: {}", JSON.toJSONString(req));
        if (StringUtil.stringEquals(req.getQueryType(), "system")) {
            // 系统预制agent(平台推荐)
            log.info("queryDefaultAgentList start.");
            return BaseResp.success(agentManagerService.queryDefaultAgentList(req));
        }
        if (StringUtil.stringEquals(req.getQueryType(), "recent")) {
            // 最近使用的agent
            log.info("queryRecentAgentList start.");
            return BaseResp.success(agentManagerService.queryRecentAgentList(req));
        }
        return BaseResp.success(agentManagerService.queryReleasedAgentList(req));
    }

    @Override
    public BaseResp<AgentListResp> queryAllAgent(AgentQueryReq req) {
        log.info("AgentManagerApiController.queryAllAgent start. req: {}", JSON.toJSONString(req));
        return BaseResp.success(agentManagerService.queryAllAgent(req));
    }

    @Override
    public BaseResp<AgentSummaryInfo> getAgentInfo(String agentId) {
        log.info("AgentManagerApiController.getAgentInfo start. agentId: {}", agentId);
        return BaseResp.success(agentManagerService.getAgentInfo(agentId));
    }
}
