/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.Agent;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 功能描述
 *
 */
public interface AgentRepository extends JpaRepository<Agent, String> {
    /**
     * findByAgentId
     * @return Agent
     */
    Agent findByAgentId(String agentId);
}

