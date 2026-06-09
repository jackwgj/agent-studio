/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 一句话语音识别接口请求体。
 */
@ApiModel(description = "一句话语音识别接口请求体。")
@Validated
public class AsrReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("config")
    @Valid
    @NotNull
    private AsrConfig config = null;

    @JsonProperty("data")
    @NotBlank
    private String data = null;

    public AsrConfig getConfig() {
        return config;
    }

    public AsrReq setConfig(AsrConfig config) {
        this.config = config;
        return this;
    }

    public String getData() {
        return data;
    }

    public AsrReq setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AsrReq {\n");

        sb.append("    config: ").append(toIndentedString(config)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AsrReq asrReq = (AsrReq) o;
        return Objects.equals(this.config, asrReq.config) && Objects.equals(this.data, asrReq.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, data);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
