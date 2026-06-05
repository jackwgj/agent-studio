/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RagFlowRetrieveDocInfo {

    private String id;

    private Long createTime;

    private String createDate;

    private Long updateTime;

    private String updateDate;

    private String thumbnail;

    private String kbId;

    private String parserId;

    private String sourceType;

    private String type;

    private String createdBy;

    private String name;

    private String location;

    private Long size;

    private Long tokenNum;

    private Long chunkNum;

    private Double progress;

    private String progressMsg;

    private String processBeginAt;

    private Double processDuation;

    private String run;

    private String status;

}
