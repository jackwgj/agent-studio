package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AudioSettings {
    @JsonProperty("audio_input")
    private String audioInput;

    @JsonProperty("audio_output")
    private String audioOutput;
}
