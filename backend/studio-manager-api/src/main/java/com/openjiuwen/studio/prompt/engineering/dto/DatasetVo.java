/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 数据集VO
 */
@ApiModel(description = "数据集VO")

@Validated
public class DatasetVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("dataset_id")
    private String datasetId = null;

    @JsonProperty("dataset_name")
    private String datasetName = null;

    @JsonProperty("dataset_type")
    private String datasetType = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("obs_path")
    private String obsPath = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    public String getDatasetId() {
        return datasetId;
    }

    public DatasetVo setDatasetId(String datasetId) {
        this.datasetId = datasetId;
        return this;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public DatasetVo setDatasetName(String datasetName) {
        this.datasetName = datasetName;
        return this;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public DatasetVo setDatasetType(String datasetType) {
        this.datasetType = datasetType;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DatasetVo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public DatasetVo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getObsPath() {
        return obsPath;
    }

    public DatasetVo setObsPath(String obsPath) {
        this.obsPath = obsPath;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public DatasetVo setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasetVo {\n");
        sb.append("    datasetId: ").append(toIndentedString(datasetId)).append("\n");
        sb.append("    datasetName: ").append(toIndentedString(datasetName)).append("\n");
        sb.append("    datasetType: ").append(toIndentedString(datasetType)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    obsPath: ").append(toIndentedString(obsPath)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
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
        DatasetVo datasetVo = (DatasetVo) o;
        return Objects.equals(this.datasetId, datasetVo.datasetId) && Objects.equals(this.datasetName,
            datasetVo.datasetName) && Objects.equals(this.datasetType, datasetVo.datasetType) && Objects.equals(
            this.description, datasetVo.description) && Objects.equals(this.projectId, datasetVo.projectId)
            && Objects.equals(this.obsPath, datasetVo.obsPath) && Objects.equals(this.createTime, datasetVo.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, datasetName, datasetType, description, projectId, obsPath, createTime);
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
