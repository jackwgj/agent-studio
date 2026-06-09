/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 创建知识库OBS配置请求体。
 */
@ApiModel(description = "创建知识库OBS配置请求体。")

@Validated

public class CreateKnowledgeBaseObsConfigReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("obs_bucket_name")
    @NotBlank
    private String obsBucketName = null;

    @JsonProperty("obs_input_directory")
    @NotBlank
    private String obsInputDirectory = null;

    public String getObsBucketName() {
        return obsBucketName;
    }

    public CreateKnowledgeBaseObsConfigReq setObsBucketName(String obsBucketName) {
        this.obsBucketName = obsBucketName;
        return this;
    }

    public String getObsInputDirectory() {
        return obsInputDirectory;
    }

    public CreateKnowledgeBaseObsConfigReq setObsInputDirectory(String obsInputDirectory) {
        this.obsInputDirectory = obsInputDirectory;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateKnowledgeBaseObsConfigReq {\n");

        sb.append("    obsBucketName: ").append(toIndentedString(obsBucketName)).append("\n");
        sb.append("    obsInputDirectory: ").append(toIndentedString(obsInputDirectory)).append("\n");
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
        CreateKnowledgeBaseObsConfigReq createKnowledgeBaseObsConfigReq = (CreateKnowledgeBaseObsConfigReq) o;
        return Objects.equals(this.obsBucketName, createKnowledgeBaseObsConfigReq.obsBucketName) && Objects.equals(
            this.obsInputDirectory, createKnowledgeBaseObsConfigReq.obsInputDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(obsBucketName, obsInputDirectory);
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
