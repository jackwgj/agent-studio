/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenJobDeatails implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("total_jobs")
    private int totalJobs;

    @JsonProperty("failed_jobs")
    private int failedJobs;

    @JsonProperty("finished_jobs")
    private int finishedJobs;

    @JsonProperty("running_jobs")
    private int runningJobs;

    @JsonProperty("stopped_jobs")
    private int stoppedJobs;

    @JsonProperty("data")
    private List<JiuWenProgress> data;
}
