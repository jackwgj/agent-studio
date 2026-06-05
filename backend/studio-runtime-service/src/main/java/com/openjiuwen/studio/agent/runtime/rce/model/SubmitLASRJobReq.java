/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交录音文件识别任务请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitLASRJobReq {
    /**
     * 文件访问地址
     */
    @JsonProperty("data_url")
    private String dataUrl;

    /**
     * 录音文件识别配置信息
     */
    private TranscriberConfig config;

    /**
     * 录音文件识别配置信息实体类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranscriberConfig {
        /**
         * 支持语音的格式，系统自动判断时为：auto
         */
        @JsonProperty("audio_format")
        private String audioFormat;

        /**
         * 所使用的模型特征串,通常是“语种_采样率_领域”的形式，例如chinese_8k_common
         */
        private String property;

        /**
         * 表示是否在识别结果中添加标点
         */
        @JsonProperty("add_punc")
        private String addPunc;

        /**
         * 表示是否将语音中的数字识别为阿拉伯数字
         */
        @JsonProperty("digit_norm")
        private String digitNorm;

        /**
         * 结果分析配置
         */
        @Builder.Default
        @JsonProperty("need_analysis_info")
        private AnalysisConfig needAnalysisInfo = new AnalysisConfig(true, true);

        @Data
        @Builder
        public static class AnalysisConfig {
            /**
             * 是否话术分离
             */
            private boolean diarization;

            /**
             * 是否情绪检测
             */
            private boolean emotion;
        }
    }
}
