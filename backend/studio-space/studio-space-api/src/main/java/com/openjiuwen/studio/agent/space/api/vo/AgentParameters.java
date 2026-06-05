package com.openjiuwen.studio.agent.space.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Fang Zhen on 2024/5/13.
 */
@Data
public class AgentParameters {
    private String type;

    private Map<String, Object> properties;

    private List<String> required;
}
