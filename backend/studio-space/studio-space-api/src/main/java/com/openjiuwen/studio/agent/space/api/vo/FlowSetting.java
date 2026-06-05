package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FlowSetting {
    @JsonProperty("call_method")
    private String call_method;
}
