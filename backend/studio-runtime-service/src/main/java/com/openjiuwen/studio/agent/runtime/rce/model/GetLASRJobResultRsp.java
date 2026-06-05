/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.runtime.dto.AudioResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 录音文件识别结果响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetLASRJobResultRsp {
    /**
     * 当前识别状态，WAITING 等待识别；FINISHED 识别已经完成；ERROR 识别过程中发生错误
     */
    private String status;

    /**
     * 错误信息
     */
    @JsonProperty("error_msg")
    private String errorMsg;

    /**
     * 识别结果数组
     */
    private List<Segment> segments;

    /**
     * 识别结果
     */
    @Data
    public static class Segment {
        /**
         * 识别结果
         */
        private AudioResult result;
    }
}
