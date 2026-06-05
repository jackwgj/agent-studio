/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptIterResEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 迭代任务id（主键）
     */
    @JsonProperty("id")
    private String id;

    /**
     * 提示词优化任务id（关联t_pe_op_task表的id）
     */
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 迭代轮次
     */
    @JsonProperty("iter_num")
    private Integer iterNum;

    /**
     * 原始提示词
     */
    @JsonProperty("ori_pt")
    private String oriPt;

    /**
     * 该轮迭代后的提示词
     */
    @JsonProperty("res_pt")
    private String resPt;

    /**
     * 该轮迭代后的精度
     */
    @JsonProperty("res_acc")
    private String resAcc;

    /**
     * 创建时间（时间戳）
     */
    @JsonProperty("created_time")
    private Date createdTime;
}
