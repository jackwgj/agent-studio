package com.openjiuwen.studio.agent.space.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AgentLinkFlowEntity {
    private String id;

    private String description;

    private String type; // API

    private String name;

    private ToolSchema schema;
}
