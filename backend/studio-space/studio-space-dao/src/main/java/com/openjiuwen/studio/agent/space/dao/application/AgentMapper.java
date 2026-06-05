package com.openjiuwen.studio.agent.space.dao.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.dao.entity.Agent;

import java.util.List;

public interface AgentMapper extends BaseMapper<Agent> {
    int insertBatchSomeColumn(List<Agent> agentList);

    default Agent queryAgentById(String agentId, String workspaceId) {
        LambdaQueryWrapper<Agent> agentQuery = new LambdaQueryWrapper<>();
        agentQuery.eq(Agent::getAgentId, agentId)
            .eq(Agent::getWorkspaceId, workspaceId)
            .eq(Agent::getDomainId, AuthorizationContextHolder.domainId())
            .eq(Agent::getDeleted, 0);
        return selectOne(agentQuery);
    }

    default Agent queryAgentById(String agentId) {
        LambdaQueryWrapper<Agent> agentQuery = new LambdaQueryWrapper<>();
        agentQuery.eq(Agent::getAgentId, agentId)
            .eq(Agent::getDomainId, AuthorizationContextHolder.domainId())
            .eq(Agent::getDeleted, 0);
        return selectOne(agentQuery);
    }

    default Agent queryAgentByIdType(String agentId, String type) {
        LambdaQueryWrapper<Agent> agentQuery = new LambdaQueryWrapper<>();
        agentQuery.eq(Agent::getAgentId, agentId)
            .eq(Agent::getType, type)
            .eq(Agent::getDomainId, AuthorizationContextHolder.domainId())
            .eq(Agent::getDeleted, 0);
        return selectOne(agentQuery);
    }
}
