package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Created by Fang Zhen on 2024/5/11.
 */
@Data
public class AgentResourceReq {
    @Size(max = 100)
    @JsonProperty("agent_id")
    private List<String> agentId;

    @Size(max = 100)
    @JsonProperty("task_id")
    private String taskId; // 这个是模型的service_key

    @Size(max = 100)
    @JsonProperty("task_ids")
    private List<String> taskIds; // 这个是模型的service_key

    @Valid
    @JsonProperty("req")
    private EnableReq req;
}
