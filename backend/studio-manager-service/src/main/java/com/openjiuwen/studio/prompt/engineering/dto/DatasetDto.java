/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 功能描述
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDto {
    @JsonProperty("dataset_id")
    private String datasetId;

    @JsonProperty("dataset_name")
    private String datasetName;

    @JsonProperty("dataset_type")
    private String datasetType;

    private String description;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("obs_path")
    private String obsPath;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @Override
    public String toString() {
        return "FinetuneDataset{" + "datasetId=" + datasetId + ", datasetName='" + datasetName + '\''
                + '\'' + ", datasetType='" + datasetType + '\''
                + ", description='" + description + '\'' + ", projectId='" + projectId + '\'' + ", obsPath='" + obsPath
                + '\'' + ", createTime=" + createTime + '}';
    }
}
