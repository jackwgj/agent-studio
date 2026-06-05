package com.openjiuwen.studio.agent.space.api.vo;

import lombok.Data;

/**
 * Created by Fang Zhen on 2024/5/14.
 */
@Data
public class ToolSchema {
    private String type; // function

    private AgentFunction function;
}
