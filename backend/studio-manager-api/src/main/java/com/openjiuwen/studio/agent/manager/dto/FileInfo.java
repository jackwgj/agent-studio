/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 知识文档，可用于知识库检索
 */
@ApiModel(description = "知识文档，可用于知识库检索")

@Validated

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("file_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(max = 64)
    private String fileId = null;

    @JsonProperty("project_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(max = 64)
    private String projectId = null;

    @JsonProperty("file_name")
    @Length(min = 1, max = 64)
    private String fileName = null;

    @JsonProperty("file_type")
    @Length(min = 1, max = 16)
    private String fileType = null;

    @JsonProperty("file_size")
    @Range(min = 1L, max = 99999999L)
    private Long fileSize = null;

    @JsonProperty("file_status")
    @Length(max = 64)
    private String fileStatus = null;

    @JsonProperty("failure_reason")
    @Length(max = 65535)
    private String failureReason = null;

    @JsonProperty("file_tags")
    @Valid
    @Size(max = 20)
    private List<@Length(max = 100) String> fileTags = null;

    @JsonProperty("metadata")
    @Length(max = 4096)
    private String metadata = null;

    @JsonProperty("create_time")
    @Range(min = 0L, max = 253402214400000L)
    private Long createTime = null;

    @JsonProperty("update_time")
    @Range(min = 0L, max = 253402214400000L)
    private Long updateTime = null;

    public String getFileId() {
        return fileId;
    }

    public FileInfo setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public FileInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public FileInfo setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public FileInfo setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public FileInfo setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public FileInfo setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
        return this;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public FileInfo setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public List<String> getFileTags() {
        return fileTags;
    }

    public FileInfo setFileTags(List<String> fileTags) {
        this.fileTags = fileTags;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public FileInfo setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public FileInfo setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public FileInfo setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FileInfo {\n");

        sb.append("    fileId: ").append(toIndentedString(fileId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    fileType: ").append(toIndentedString(fileType)).append("\n");
        sb.append("    fileSize: ").append(toIndentedString(fileSize)).append("\n");
        sb.append("    fileStatus: ").append(toIndentedString(fileStatus)).append("\n");
        sb.append("    failureReason: ").append(toIndentedString(failureReason)).append("\n");
        sb.append("    fileTags: ").append(toIndentedString(fileTags)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) obj;
        return Objects.equals(this.fileId, fileInfo.fileId) && Objects.equals(this.projectId, fileInfo.projectId)
            && Objects.equals(this.fileName, fileInfo.fileName) && Objects.equals(this.fileType, fileInfo.fileType)
            && Objects.equals(this.fileSize, fileInfo.fileSize) && Objects.equals(this.fileStatus, fileInfo.fileStatus)
            && Objects.equals(this.failureReason, fileInfo.failureReason) && Objects.equals(this.fileTags,
            fileInfo.fileTags) && Objects.equals(this.metadata, fileInfo.metadata) && Objects.equals(this.createTime,
            fileInfo.createTime) && Objects.equals(this.updateTime, fileInfo.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, projectId, fileName, fileType, fileSize, fileStatus, failureReason, fileTags,
            metadata, createTime, updateTime);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
