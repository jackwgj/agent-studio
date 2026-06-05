package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
public class AgentBuilderFileExtConfigVo {
    @JsonProperty("is_template")
    private boolean template;
}
