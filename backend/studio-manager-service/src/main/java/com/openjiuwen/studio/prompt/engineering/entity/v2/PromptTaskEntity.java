/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.TargetTypeEnum;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识一个提示词优化任务
     */
    @JsonProperty("id")
    private String id;

    /**
     * 唯一标识一个提示词优化任务
     */
    @JsonProperty("jiuwen_task_id")
    private String jiuwenTaskId;

    /**
     * 提示词优化任务名称
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
     * 提示词优化任务类型，text文本类型，multi多模态
     */
    @JsonProperty("pt_type")
    private String ptType;

    /**
     * 优化文本
     */
    @JsonProperty("pt_text")
    private String ptText;

    /**
     * 提示词变量
     */
    @JsonProperty("pt_vars")
    private String ptVars;

    /**
     * 提示词优化任务执行时间
     */
    @JsonProperty("exec_time")
    private Date execTime;

    /**
     * 优化提示词模型
     */
    @JsonProperty("pt_model")
    private String ptModel;

    /**
     * 提示词优化任务执行对象
     */
    @JsonProperty("exec_object")
    private String execObject;

    /**
     * 提示词优化任务最大迭代次数
     */
    @JsonProperty("max_iter_num")
    private Integer maxIterNum;

    /**
     * 提示词优化任务目标精度
     */
    @JsonProperty("target_acc")
    private String targetAcc;

    /**
     * 终提示词里的示例个数，最多为用例集里的用例个数
     */
    @JsonProperty("show_case_num")
    private Integer showCaseNum;

    /**
     * 用例的目标类型，objective客观任务，subjective主观任务
     */
    @JsonProperty("target_type")
    private String targetType;

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
     * 运行状态，0草稿、1等待执行、2执行中、3执行成功、4执行失败
     */
    @JsonProperty("status")
    private int status;

    /**
     * 任务运行失败信息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 项目id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 空间id
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 创建时间
     */
    @JsonProperty("created_time")
    private Date createdTime;

    /**
     * 工具创建者
     */
    @JsonProperty("creator")
    private String creator;

    /**
     * 工具创建者user id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 租户id
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_time")
    private Date updatedTime;

    /**
     * 优化算法
     */
    @JsonProperty("algorithm")
    private String algorithm;

    public PromptTaskDetailVo convert2PromptTaskDetailVo() {
        PromptTaskDetailVo.PromptTaskDetailVoBuilder builder = PromptTaskDetailVo.builder();
        builder.id(this.getId())
            .jiuwenTaskId(this.getJiuwenTaskId())
            .name(this.getName())
            .desc(this.getDesc())
            .promptId(this.getPromptId())
            .ptText(this.getPtText())
            .execTime(this.getExecTime())
            .scoreStandard(this.getScoreStandard())
            .backKnowledge(this.getBackKnowledge())
            .progressRate(this.getProgressRate())
            .status(PromptTaskStatusEnum.getByCode(this.getStatus()))
            .message(this.getMessage())
            .workspaceId(this.getWorkspaceId())
            .projectId(this.getProjectId())
            .creator(this.getCreator())
            .creatorId(this.getCreatorId())
            .domainId(this.getDomainId())
            .algorithm(this.getAlgorithm());
        builder.ptType(getPtTypeValue());
        builder.execObject(getExecObjectValue());
        builder.ptVars(getPtVarsValue());
        builder.ptModel(getPtModelValue());
        builder.targetAcc(getTargetAccValue());
        builder.maxIterNum(this.getMaxIterNum() != null ? this.getMaxIterNum() : 0);
        builder.showCaseNum(this.getShowCaseNum() != null ? this.getShowCaseNum() : 0);
        builder.targetType(getTargetTypeValue());
        builder.createdTime(getCreatedTimeValue());
        builder.updatedTime(getUpdatedTimeValue());
        return builder.build();
    }

    private PtTypeEnum getPtTypeValue() {
        return StringUtils.isNotBlank(this.getPtType()) ? PtTypeEnum.fromValue(this.getPtType()) : null;
    }

    private ExecConfig getExecObjectValue() {
        return StringUtils.isNotBlank(this.getExecObject()) 
            ? JsonUtils.json2ObjQuietly(this.getExecObject(), ExecConfig.class) : null;
    }

    private List<PromptVar> getPtVarsValue() {
        return this.getPtVars() != null 
            ? JsonUtils.json2Obj(this.getPtVars(), new TypeReference<List<PromptVar>>() { }) 
            : Collections.emptyList();
    }

    private ExecConfig getPtModelValue() {
        return this.getPtModel() != null 
            ? JsonUtils.json2ObjQuietly(this.getPtModel(), ExecConfig.class) 
            : null;
    }

    private double getTargetAccValue() {
        return StringUtils.isNotBlank(this.getTargetAcc()) ? Double.valueOf(this.getTargetAcc()) : 0.0;
    }

    private TargetTypeEnum getTargetTypeValue() {
        return StringUtils.isNotBlank(this.getTargetType()) ? TargetTypeEnum.fromValue(this.getTargetType()) : null;
    }

    private long getCreatedTimeValue() {
        return this.getCreatedTime() != null ? this.getCreatedTime().getTime() : 0L;
    }

    private long getUpdatedTimeValue() {
        return this.getUpdatedTime() != null ? this.getUpdatedTime().getTime() : 0L;
    }
}
