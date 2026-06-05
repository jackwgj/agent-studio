package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnableReq {
    @JsonProperty("key")
    private String apiKey;

    @JsonProperty("type")
    @NotBlank
    private String uriType;

    @JsonProperty("path")
    private String uriPrefix;

    @JsonProperty("instance_size")
    private String instanceSize;

    @JsonProperty("instance_amount")
    private String instanceAmount;
}
