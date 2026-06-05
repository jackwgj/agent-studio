/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 图片上传包装类
 */
@ApiModel(description = "图片上传包装类")

@Validated
public class FileInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("file_id")
    @NotBlank
    private String fileId = null;

    @JsonProperty("tempUrl")
    @NotBlank
    private String tempUrl = null;

    public String getFileId() {
        return fileId;
    }

    public FileInfoVo setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getTempUrl() {
        return tempUrl;
    }

    public FileInfoVo setTempUrl(String tempUrl) {
        this.tempUrl = tempUrl;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FileInfoVo {\n");
        sb.append("    fileId: ").append(toIndentedString(fileId)).append("\n");
        sb.append("    tempUrl: ").append(toIndentedString(tempUrl)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfoVo fileInfoVo = (FileInfoVo) o;
        return Objects.equals(this.fileId, fileInfoVo.fileId) && Objects.equals(this.tempUrl, fileInfoVo.tempUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, tempUrl);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
