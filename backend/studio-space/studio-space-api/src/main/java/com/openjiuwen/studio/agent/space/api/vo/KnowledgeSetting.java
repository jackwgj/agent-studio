package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KnowledgeSetting {
    @JsonProperty("call_method")
    private String callMethod;

    @JsonProperty("maximum_recalls")
    private Integer maximumRecalls;

    @JsonProperty("similarity_min")
    private double similarityMin;

    @JsonProperty("knowledge_prompt")
    private String knowledgePrompt;

    @JsonProperty("prompt_conf")
    private String promptConf;

    @JsonProperty("retrieval_method")
    private String retrievalMethod;
}
