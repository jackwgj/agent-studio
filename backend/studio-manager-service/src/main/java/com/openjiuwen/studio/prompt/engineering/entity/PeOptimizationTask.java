/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskVo;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;
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
 * 优化任务
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeOptimizationTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * UUID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 工程id
     */
    private String taskId;

    /**
     * 优化任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型
     */
    private String type;

    /**
     * 九问任务ID
     */
    private String jiuwenTaskId;

    /**
     * 选择的候选模板
     */
    private List<PePrompt> prompts;

    /**
     * 数据集ID
     */
    private String testSetId;

    /**
     * 优化模型
     */
    private ModelConfig modelConfig;

    /**
     * 评分模型
     */
    private ModelConfig assistantConfig;

    /**
     * 迭代轮数
     */
    private int numIter;

    /**
     * 优化后的模板
     */
    private String bestPrompt;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 进度
     */
    private double progressRate;

    /**
     * 优化任务状态
     */
    private OptimizationTaskStatus status;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 优化创建时间
     */
    private Date createdOn;

    /**
     * 优化启动创建时间
     */
    private Date runningOn;

    /**
     * 优化更新时间
     */
    private Date updatedOn;

    private String workspaceId;

    public PeOptimizationTaskVo convertToPeOptimizationTaskVo() {
        PeOptimizationTaskToPeOptimizationTaskVoConverter toPeOptimizationTaskVoConverter =
            new PeOptimizationTaskToPeOptimizationTaskVoConverter();
        return toPeOptimizationTaskVoConverter.convert(this);
    }

    private static class PeOptimizationTaskToPeOptimizationTaskVoConverter
        implements FormConvert<PeOptimizationTask, PeOptimizationTaskVo> {
        @Override
        public PeOptimizationTaskVo convert(PeOptimizationTask peOptimizationTask) {
            if (peOptimizationTask.getModelConfig() != null) {
                peOptimizationTask.getModelConfig().setToken(null);
            }
            if (peOptimizationTask.getAssistantConfig() != null) {
                peOptimizationTask.getAssistantConfig().setToken(null);
            }
            PeOptimizationTaskVo peOptimizationTaskVo = new PeOptimizationTaskVo();
            peOptimizationTaskVo.setId(peOptimizationTask.getId());
            peOptimizationTaskVo.setName(peOptimizationTask.getName());
            peOptimizationTaskVo.setDescription(peOptimizationTask.getDescription());
            peOptimizationTaskVo.setType(peOptimizationTask.getType());
            peOptimizationTaskVo.setJiuwenTaskId(peOptimizationTask.getJiuwenTaskId());
            peOptimizationTaskVo.setTaskId(peOptimizationTask.getTaskId());
            peOptimizationTaskVo.setTestSetId(peOptimizationTask.getTestSetId());
            peOptimizationTaskVo.setModelConfig(peOptimizationTask.getModelConfig());
            peOptimizationTaskVo.setAssistantConfig(peOptimizationTask.getAssistantConfig());
            peOptimizationTaskVo.setNumIter(peOptimizationTask.getNumIter());
            peOptimizationTaskVo.setStatus(peOptimizationTask.getStatus().getCode());
            peOptimizationTaskVo.setProgressRate(peOptimizationTask.getProgressRate());
            peOptimizationTaskVo.setBestPrompt(peOptimizationTask.getBestPrompt());
            peOptimizationTaskVo.setErrorMsg(peOptimizationTask.getErrorMsg());
            peOptimizationTaskVo.setPrompts(peOptimizationTask.getPrompts()
                .stream()
                .map(PePrompt::convertToPePromptVO)
                .collect(Collectors.toList()));
            peOptimizationTaskVo.setCreatedOn(DateUtil.format(peOptimizationTask.getCreatedOn()));
            peOptimizationTaskVo.setUpdatedOn(DateUtil.format(peOptimizationTask.getUpdatedOn()));
            peOptimizationTaskVo.setRunningOn(DateUtil.format(peOptimizationTask.getRunningOn()));
            peOptimizationTaskVo.setCreator(peOptimizationTask.getCreator());
            return peOptimizationTaskVo;
        }
    }
}
