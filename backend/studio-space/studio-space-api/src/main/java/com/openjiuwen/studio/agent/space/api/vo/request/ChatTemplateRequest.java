/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DR 模板地址
 */
@Data
@Accessors(chain = true)
public class ChatTemplateRequest implements Serializable {

    // 静态代码检查G.SER.02：实现Serializable的类应显式声明serialVersionUID，避免类变更后反序列化兼容性问题
    private static final long serialVersionUID = 1L;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("is_template")
    private Boolean isTemplate;
}
