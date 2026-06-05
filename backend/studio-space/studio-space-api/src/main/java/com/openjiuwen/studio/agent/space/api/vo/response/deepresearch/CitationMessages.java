package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import lombok.Data;

import java.util.List;

@Data
public class CitationMessages {
    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 引用消息列表
     */
    private List<CitationMessage> data;
}
