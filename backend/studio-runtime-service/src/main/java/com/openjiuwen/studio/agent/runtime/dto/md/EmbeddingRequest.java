/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import com.openjiuwen.studio.agent.common.validator.ValidEmbeddingObject;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmbeddingRequest extends ModelInvokeBase {
    /**
     * Input text to get embeddings for, encoded as a string or array of tokens.
     * To get embeddings for multiple inputs in a single request, pass an array of strings or array of token arrays.
     * Each input must not exceed 2048 tokens in length.
     * <p>
     * Unless you are embedding code, we suggest replacing newlines (\n) in your input with a single space,
     * as we have observed inferior results when newlines are present.
     */
    @ValidEmbeddingObject
    @NotNull(message = "Parameter 'input' must be not null, please check and try again later!")
    private Object input;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
     */
    @Size(max = 1000)
    private String user;

    private TruncateType truncate = TruncateType.none;
}
