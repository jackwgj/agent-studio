/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvaluationTaskVo;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationMethodType;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.utils.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估任务
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeEvaluationTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * UUID
     */
    private String id;

    /**
     * 评估任务名称
     */
    private String name;

    /**
     * 评估方法
     */
    private EvaluationMethodType method;

    /**
     * 工程id
     */
    private String taskId;

    /**
     * 选择的评估用例集Id
     */
    private String testSetId;

    /**
     * 用例集名称
     */
    private String testSetName;

    /**
     * 用例数
     */
    private int caseNum;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 评估正则表达式
     */
    private String regularExpression;

    /**
     * 模型评估配置
     */
    private ModelConfig evalModelConfig;

    /**
     * 评估创建时间
     */
    private Date createdOn;

    /**
     * 评估创建时间
     */
    private Date updatedOn;

    /**
     * 评估任务状态
     */
    private EvaluationTaskStatus status;

    /**
     * 选择的候选模板
     */
    private List<PePrompt> prompts;

    /**
     * 最佳候选
     */
    private String promptBestName;

    /**
     * 最佳候选评分
     */
    private Integer bestScore;

    /**
     * 创建人
     */
    private String creator;

    private String workspaceId;

    public PeEvaluationTaskVo convertToPeEvaluationTaskVo() {
        PeEvaluationTaskToPeEvaluationTaskVoConverter toPeEvaluationTaskVoConverter = new PeEvaluationTaskToPeEvaluationTaskVoConverter();
        return toPeEvaluationTaskVoConverter.convert(this);
    }

    private static class PeEvaluationTaskToPeEvaluationTaskVoConverter
            implements FormConvert<PeEvaluationTask, PeEvaluationTaskVo> {
        @Override
        public PeEvaluationTaskVo convert(PeEvaluationTask peEvaluationTask) {
            PeEvaluationTaskVo peEvaluationTaskVo = new PeEvaluationTaskVo();
            peEvaluationTaskVo.setId(peEvaluationTask.getId());
            peEvaluationTaskVo.setName(peEvaluationTask.getName());
            peEvaluationTaskVo.setDescription(peEvaluationTask.getDescription());
            peEvaluationTaskVo.setMethod(peEvaluationTask.getMethod().getMethod());
            peEvaluationTaskVo.setTaskId(peEvaluationTask.getTaskId());
            peEvaluationTaskVo.setTestSetId(peEvaluationTask.getTestSetId());
            peEvaluationTaskVo.setTestSetName(peEvaluationTask.getTestSetName());
            peEvaluationTaskVo.setCaseNum(peEvaluationTask.getCaseNum());
            peEvaluationTaskVo.setRegularExpression(peEvaluationTask.getRegularExpression());
            peEvaluationTaskVo.setCreatedOn(DateUtil.format(peEvaluationTask.getCreatedOn()));
            peEvaluationTaskVo.setUpdatedOn(DateUtil.format(peEvaluationTask.getUpdatedOn()));
            peEvaluationTaskVo.setStatus(peEvaluationTask.getStatus().name());
            peEvaluationTaskVo.setPromptList(peEvaluationTask.getPrompts().stream().map(PePrompt::convertToPePromptVO)
                            .collect(Collectors.toList()));
            peEvaluationTaskVo.setPromptBestName(peEvaluationTask.getPromptBestName());
            peEvaluationTaskVo.setPromptNum(peEvaluationTask.getPrompts().size());
            peEvaluationTaskVo.setBestScore(peEvaluationTask.getBestScore());
            peEvaluationTaskVo.setCreator(peEvaluationTask.getCreator());
            return peEvaluationTaskVo;
        }
    }
}
