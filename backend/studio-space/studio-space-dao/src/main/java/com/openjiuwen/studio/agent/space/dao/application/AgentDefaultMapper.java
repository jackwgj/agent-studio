package com.openjiuwen.studio.agent.space.dao.application;

import com.openjiuwen.studio.agent.space.dao.entity.AgentDefault;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface AgentDefaultMapper extends BaseMapper<AgentDefault> {

    /**
     * 查询所有agentId
     */
    default List<String> selectAgentIds() {
        LambdaQueryWrapper<AgentDefault> agentQuery = new LambdaQueryWrapper<>();
        agentQuery.select(AgentDefault::getId);
        List<AgentDefault> agentList = selectList(agentQuery);
        return agentList.stream().map(AgentDefault::getAgentId).toList();
    }

    /**
     * 根据id查询agent
     */
    default AgentDefault queryAgentIds(String agentId) {
        LambdaQueryWrapper<AgentDefault> agentQuery = new LambdaQueryWrapper<>();
        agentQuery.eq(AgentDefault::getAgentId, agentId);
        List<AgentDefault> agentList = selectList(agentQuery);
        if(agentList.isEmpty()){
            return null;
        }
        return agentList.get(0);
    }
}
