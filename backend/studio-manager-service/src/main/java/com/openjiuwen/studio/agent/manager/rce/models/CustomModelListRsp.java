/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import lombok.Data;

import java.util.List;

/**
 * 模型列表接口响应体对象
 *
 */
@Data
public class CustomModelListRsp {
    private String code;

    private String msg;

    private List<Result> result;

    @Data
    public static class Result {
        private String modelNameEn;

        private String modelNameCN;

        private ModelConf modelConf;

        @Data
        public static class ModelConf {
            private String url;

            private String token;

            private String userId;

            private String supportFunctionCall;
        }
    }
}
