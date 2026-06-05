/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OCR智能文档识别请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmartDocumentRecognizerRsp {
    private List<Result> result;

    /**
     * 调用成功时返回的识别结果
     */
    @Data
    public static class Result {
        @JsonProperty("ocr_result")
        private OcrInfo ocrResult;

        @JsonProperty("table_result")
        private TableInfo tableResult;
    }

    /**
     * 文字识别结果
     */
    @Data
    public static class OcrInfo {
        @JsonProperty("words_block_count")
        private int wordsBlockCount;

        @JsonProperty("words_block_list")
        private List<WordsBlock> wordsBlockList;

        @Data
        public static class WordsBlock {
            private int[][] location;

            private String words;

            private float confidence;
        }
    }

    /**
     * 表格识别结果。当输入参数table为true时，才返回该参数
     */
    @Data
    public static class TableInfo {
        @JsonProperty("table_count")
        private int tableCount;

        @JsonProperty("table_list")
        private List<TableBlock> tableList;

        @Data
        public static class TableBlock {
            private int[][] location;

            @JsonProperty("words_block_count")
            private int wordsBlockCount;

            @JsonProperty("words_block_list")
            private List<TableWordsBlock> wordsBlockList;

            @Data
            public static class TableWordsBlock {
                private String words;

                private int[] rows;

                private int[] columns;
            }
        }
    }
}
