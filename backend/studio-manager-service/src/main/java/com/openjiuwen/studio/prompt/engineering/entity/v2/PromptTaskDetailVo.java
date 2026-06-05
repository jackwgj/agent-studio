/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.TargetTypeEnum;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 提示词优化任务详情
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTaskDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提示词优化任务id
     */
    @JsonProperty("id")
    private String id;

    /**
     * jiuwen提示词优化任务id
     */
    @JsonProperty("jiuwen_task_id")
    private String jiuwenTaskId;

    /**
     * 提示词优化任务名
     */
    @JsonProperty("name")
    private String name;

    /**
     * 提示词优化任务描述
     */
    @JsonProperty("desc")
    private String desc;

    /**
     * 关联的“我的提示词”模板id
     */
    @JsonProperty("prompt_id")
    private String promptId;

    /**
     * 提示词优化任务类型,多模态任务和文本任务
     */
    @JsonProperty("pt_type")
    private PtTypeEnum ptType;

    /**
     * 提示词优化任务提示词
     */
    @JsonProperty("pt_text")
    private String ptText;

    /**
     * 提示词优化任务变量
     */
    @JsonProperty("pt_vars")
    private List<PromptVar> ptVars;

    /**
     * 提示词优化任务执行时间
     */
    @JsonProperty("exec_time")
    private Date execTime;

    /**
     * 提示词优化任务优化模型
     */
    @JsonProperty("pt_model_config")
    private ExecConfig ptModel;

    /**
     * 提示词优化任务推理模型
     */
    @JsonProperty("exec_object_config")
    private ExecConfig execObject;

    /**
     * 迭代轮次
     */
    @JsonProperty("max_iter_num")
    private Integer maxIterNum;

    /**
     * 目标准确率
     */
    @JsonProperty("target_acc")
    private double targetAcc;

    /**
     * 展示case数
     */
    @JsonProperty("show_case_num")
    private Integer showCaseNum;

    /**
     * 任务目标类型
     */
    @JsonProperty("target_type")
    private TargetTypeEnum targetType;

    /**
     * 评分标准
     */
    @JsonProperty("score_standard")
    private String scoreStandard;

    /**
     * 背景知识
     */
    @JsonProperty("back_knowledge")
    private String backKnowledge;

    /**
     * 运行进度
     */
    @JsonProperty("progress_rate")
    private double progressRate;

    /**
     * 运行状态
     */
    @JsonProperty("status")
    private PromptTaskStatusEnum status;

    /**
     * 任务运行失败信息
     */
    @JsonProperty("message")
    private String message;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("created_time")
    private long createdTime;

    @JsonProperty("creator")
    private String creator;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("updatedTime")
    private long updatedTime;

    /**
     * 租户id
     */
    @JsonProperty("domain_id")
    private String domainId;


    /**
     * 优化算法
     */
    @JsonProperty("algorithm")
    private String algorithm;

    /**
     * 每轮的结果
     */
    @JsonProperty("iteration_results")
    private PromptIterationResultVo iterationResult;

    public PromptTaskEntity convert2PromptTaskEntity() {
        return PromptTaskEntity.builder()
            .id(this.getId())
            .jiuwenTaskId(this.getJiuwenTaskId())
            .name(this.getName())
            .desc(this.getDesc())
            .promptId(this.getPromptId())
            .ptType(this.getPtType().toString())
            .execObject(JsonUtils.toJson(this.getExecObject()))
            .ptText(this.getPtText())
            .ptVars(JsonUtils.toJson(this.getPtVars()))
            .ptModel(JsonUtils.toJson(this.getPtModel()))
            .execTime(this.getExecTime())
            .targetAcc(String.valueOf(this.getTargetAcc()))
            .maxIterNum(this.getMaxIterNum())
            .showCaseNum(this.getShowCaseNum())
            .targetType(this.getTargetType().toString())
            .scoreStandard(this.getScoreStandard())
            .backKnowledge(this.getBackKnowledge())
            .progressRate(this.getProgressRate())
            .status(this.getStatus().getCode()) // 草稿状态
            .message(this.getMessage())
            .createdTime(new Date(this.getCreatedTime()))
            .updatedTime(new Date(this.getUpdatedTime()))
            .workspaceId(this.getWorkspaceId())
            .projectId(this.getProjectId())
            .creator(this.getCreator())
            .creatorId(this.getCreatorId())
            .domainId(this.getDomainId())
            .algorithm(this.getAlgorithm())
            .build();
    }
}
