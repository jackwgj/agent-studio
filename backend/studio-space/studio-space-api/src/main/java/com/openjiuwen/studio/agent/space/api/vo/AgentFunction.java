package com.openjiuwen.studio.agent.space.api.vo;

import lombok.Data;

/**
 * Created by Fang Zhen on 2024/5/13.
 */
@Data
public class AgentFunction {
    private String name;

    private String description;

    private AgentParameters parameters;
}
