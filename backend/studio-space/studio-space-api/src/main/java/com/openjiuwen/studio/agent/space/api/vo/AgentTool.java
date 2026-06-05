package com.openjiuwen.studio.agent.space.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Fang Zhen on 2024/5/13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTool {
    private String id;

    private String type; // API

    private String name;

    private ToolSchema schema;
}
