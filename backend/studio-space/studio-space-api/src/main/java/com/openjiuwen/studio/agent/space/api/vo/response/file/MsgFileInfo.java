package com.openjiuwen.studio.agent.space.api.vo.response.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderFileExtConfigVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgFileInfo {
    @JsonProperty("msg_id")
    String msgId;

    @JsonProperty("task_id")
    String taskId;

    @JsonProperty("file_id")
    String fileId;

    @JsonProperty("file_name")
    String fileName;

    @JsonProperty("download_url")
    String downloadUrl;

    @JsonProperty("source")
    private String source;

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("extra_config")
    AgentBuilderFileExtConfigVo extConfig;
}
