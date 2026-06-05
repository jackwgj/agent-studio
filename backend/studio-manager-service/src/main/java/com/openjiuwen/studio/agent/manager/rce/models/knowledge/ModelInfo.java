/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * 列举模型信息响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ModelInfo {
    @JsonProperty("name")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-_]{0,31}$")
    @NotBlank
    @Length(min = 1, max = 32)
    private String name;

    @JsonProperty("endpoint")
    @Pattern(regexp = "^.*$")
    @Length(min = 1, max = 512)
    private String endpoint;

    @JsonProperty("path")
    @Pattern(regexp = "^[a-zA-Z0-9_/-]*$")
    @Length(min = 1, max = 512)
    private String path;

    @JsonProperty("detail")
    @Length(max = 512)
    private String detail;

    @JsonProperty("type")
    @Pattern(regexp = "^.*$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String type;

    @JsonProperty("status")
    @Length(max = 16)
    private String status;

    @JsonProperty("url")
    @Pattern(regexp = "^(http|https)://(?![^/]*@)(?!.*\\.\\.)[^ ]*$")
    @Length(min = 8, max = 256)
    private String url;

    @JsonProperty("custom_header")
    @Valid
    @Size()
    private Map<@Length() String, @Length() String> customHeader;
}
