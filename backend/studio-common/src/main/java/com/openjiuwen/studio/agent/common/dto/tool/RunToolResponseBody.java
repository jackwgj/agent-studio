/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 运行工具结果返回。
 */
@ApiModel(description = "运行工具结果返回。")
@Validated
public class RunToolResponseBody {
    @JsonProperty("raw_request_url")
    @NotBlank
    private String rawRequestUrl = null;

    @JsonProperty("raw_request_headers")
    @Valid
    @NotNull
    @Size()
    private Map<@Length() String, @Length() String> rawRequestHeaders = new HashMap<>();

    @JsonProperty("raw_request_body")
    @NotBlank
    @Length(max = 10000000)
    private String rawRequestBody = null;

    @JsonProperty("raw_response_code")
    @NotNull
    private Integer rawResponseCode = null;

    @JsonProperty("raw_response")
    @NotBlank
    @Length(max = 100000)
    private String rawResponse = null;

    @JsonProperty("success")
    @NotNull
    private Boolean success = null;

    @JsonProperty("errorMessage")
    @NotBlank
    private String errorMessage = null;

    public String getRawRequestUrl() {
        return rawRequestUrl;
    }

    public RunToolResponseBody setRawRequestUrl(String rawRequestUrl) {
        this.rawRequestUrl = rawRequestUrl;
        return this;
    }

    public Map<String, String> getRawRequestHeaders() {
        return rawRequestHeaders;
    }

    public RunToolResponseBody setRawRequestHeaders(Map<String, String> rawRequestHeaders) {
        this.rawRequestHeaders = rawRequestHeaders;
        return this;
    }

    public String getRawRequestBody() {
        return rawRequestBody;
    }

    public RunToolResponseBody setRawRequestBody(String rawRequestBody) {
        this.rawRequestBody = rawRequestBody;
        return this;
    }

    public Integer getRawResponseCode() {
        return rawResponseCode;
    }

    public RunToolResponseBody setRawResponseCode(Integer rawResponseCode) {
        this.rawResponseCode = rawResponseCode;
        return this;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public RunToolResponseBody setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
        return this;
    }

    public RunToolResponseBody setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RunToolResponseBody setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RunToolResponseBody {\n");

        sb.append("    rawRequestUrl: ").append(toIndentedString(rawRequestUrl)).append("\n");
        sb.append("    rawRequestHeaders: ").append(toIndentedString(rawRequestHeaders)).append("\n");
        sb.append("    rawRequestBody: ").append(toIndentedString(rawRequestBody)).append("\n");
        sb.append("    rawResponseCode: ").append(toIndentedString(rawResponseCode)).append("\n");
        sb.append("    rawResponse: ").append(toIndentedString(rawResponse)).append("\n");
        sb.append("    success: ").append(toIndentedString(success)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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
        RunToolResponseBody runToolResponseBody = (RunToolResponseBody) o;
        return Objects.equals(this.rawRequestUrl, runToolResponseBody.rawRequestUrl) && Objects.equals(
            this.rawRequestHeaders, runToolResponseBody.rawRequestHeaders) && Objects.equals(this.rawRequestBody,
            runToolResponseBody.rawRequestBody) && Objects.equals(this.rawResponseCode,
            runToolResponseBody.rawResponseCode) && Objects.equals(this.rawResponse, runToolResponseBody.rawResponse)
            && Objects.equals(this.success, runToolResponseBody.success) && Objects.equals(this.errorMessage,
            runToolResponseBody.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawRequestUrl, rawRequestHeaders, rawRequestBody, rawResponseCode, rawResponse, success,
            errorMessage);
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
