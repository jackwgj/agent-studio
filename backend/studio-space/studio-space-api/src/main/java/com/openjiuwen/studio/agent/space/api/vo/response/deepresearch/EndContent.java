package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EndContent {
    /**
     * 最终报告内容
     */
    @JsonProperty("response_content")
    private String responseContent;

    /**
     * 溯源信息
     */
    @JsonProperty("citation_messages")
    private CitationMessages citationMessages;

    /**
     * 异常信息
     */
    @JsonProperty("exception_info")
    private String exceptionInfo;

    /**
     * 文件ID
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * 告警信息：主图WorkFlow执行过程中的告警信息
     */
    @JsonProperty("warning_info")
    private String warningInfo;
}
