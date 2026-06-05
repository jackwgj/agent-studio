
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptResultVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 候选模板
 *
 * @TableName t_pe_prompt
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PePrompt implements Serializable {
    /**
     * UUID
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 模型参数{"temperature":0.8,"presence_penalty":0}
     */
    private ModelConfig modelConfig;

    /**
     * 所属工程任务id
     */
    private String taskId;

    /**
     * 来源（模型调优、playground调优、实验调优、无来源）
     */
    private String source;

    /**
     * 0标识历史，1标识候选
     */
    private int type;

    /**
     * question
     */
    private String question;

    /**
     * answer
     */
    private String answer;

    /**
     * 模板信息，[{"id":"id","name":"name","value":"value","task_id":"task_id"}]
     */
    private String variables;

    /**
     * 人工打分
     */
    private Integer manualScore;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;

    private String workspaceId;

    private String fileId;

    private static final long serialVersionUID = 1L;

    public static PePrompt convertToPePrompt(PePromptDto promptDTO) {
        PePromptDTOToPePromptConverter toPePromptConverter = new PePromptDTOToPePromptConverter();
        return toPePromptConverter.convert(promptDTO);
    }

    public PePromptVo convertToPePromptVO() {
        PePromptToPePromptVOConverter toPePromptVOConverter = new PePromptToPePromptVOConverter();
        return toPePromptVOConverter.convert(this);
    }

    public PePromptResultVo convertToPePromptResultVO() {
        PePromptToPePromptResultVOConverter toPePromptResultVOConverter = new PePromptToPePromptResultVOConverter();
        return toPePromptResultVOConverter.convert(this);
    }

    private static class PePromptDTOToPePromptConverter implements FormConvert<PePromptDto, PePrompt> {
        @Override
        public PePrompt convert(PePromptDto pePromptDTO) {
            return PePrompt.builder()
                .id(StringUtils.isEmpty(pePromptDTO.getId()) ? UUID.randomUUID().toString() : pePromptDTO.getId())
                .name(pePromptDTO.getName())
                .model(pePromptDTO.getModel())
                .modelConfig(pePromptDTO.getModelConfig())
                .taskId(pePromptDTO.getTaskId())
                .source(pePromptDTO.getSource())
                .type(pePromptDTO.getType())
                .question(pePromptDTO.getQuestion())
                .answer(pePromptDTO.getAnswer())
                .manualScore(pePromptDTO.getManualScore())
                .fileId(pePromptDTO.getFileId())
                .build();
        }
    }

    private static class PePromptToPePromptVOConverter implements FormConvert<PePrompt, PePromptVo> {
        @Override
        public PePromptVo convert(PePrompt pePrompt) {
            PePromptVo pePromptVo = new PePromptVo();
            pePromptVo.setId(pePrompt.getId());
            pePromptVo.setName(pePrompt.getName());
            pePromptVo.setContent(pePrompt.getQuestion());
            pePromptVo.setAnswer(pePrompt.getAnswer());
            pePromptVo.setModel(pePrompt.getModel());
            pePromptVo.setModelConfig(pePrompt.getModelConfig());
            pePromptVo.setManualScore(pePrompt.getManualScore());
            pePromptVo.setCreator(pePrompt.getCreator());
            pePromptVo.setUpdater(pePrompt.getUpdater());
            pePromptVo.setCreatedOn(pePrompt.getCreatedOn());
            pePromptVo.setUpdatedOn(pePrompt.getUpdatedOn());
            return pePromptVo;
        }
    }

    private static class PePromptToPePromptResultVOConverter implements FormConvert<PePrompt, PePromptResultVo> {
        @Override
        public PePromptResultVo convert(PePrompt pePrompt) {
            PePromptResultVo pePromptResultVo = new PePromptResultVo();
            pePromptResultVo.setId(pePrompt.getId());
            pePromptResultVo.setName(pePrompt.getName());
            pePromptResultVo.setContent(pePrompt.getQuestion());
            pePromptResultVo.setModel(pePrompt.getModel());
            pePromptResultVo.setModelConfig(pePrompt.getModelConfig());
            pePromptResultVo.setManualScore(pePrompt.getManualScore());
            pePromptResultVo.setCreator(pePrompt.getCreator());
            pePromptResultVo.setUpdater(pePrompt.getUpdater());
            pePromptResultVo.setCreatedOn(pePrompt.getCreatedOn());
            pePromptResultVo.setUpdatedOn(pePrompt.getUpdatedOn());
            return pePromptResultVo;
        }
    }
}
