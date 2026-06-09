/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * PePromptResultVo
 */

@Validated
public class PePromptResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("model")
    private String model = null;

    @JsonProperty("manual_score")
    private Integer manualScore = null;

    @JsonProperty("model_config")
    @Valid
    private ModelConfig modelConfig = null;

    @JsonProperty("variable_vo")
    @Valid
    private List<VariableVo> variableVo = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("updater")
    private String updater = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    @JsonProperty("score")
    private Integer score = null;

    @JsonProperty("token")
    private Integer token = null;

    @JsonProperty("correct_num")
    private Integer correctNum = null;

    @JsonProperty("error_num")
    private Integer errorNum = null;

    @JsonProperty("time")
    private Double time = null;

    @JsonProperty("file_info")
    @Valid
    private FileInfoVo fileInfo = null;

    public String getId() {
        return id;
    }

    public PePromptResultVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PePromptResultVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PePromptResultVo setContent(String content) {
        this.content = content;
        return this;
    }

    public String getModel() {
        return model;
    }

    public PePromptResultVo setModel(String model) {
        this.model = model;
        return this;
    }

    public Integer getManualScore() {
        return manualScore;
    }

    public PePromptResultVo setManualScore(Integer manualScore) {
        this.manualScore = manualScore;
        return this;
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public PePromptResultVo setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        return this;
    }

    public List<VariableVo> getVariableVo() {
        return variableVo;
    }

    public PePromptResultVo setVariableVo(List<VariableVo> variableVo) {
        this.variableVo = variableVo;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public PePromptResultVo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public PePromptResultVo setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public PePromptResultVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public PePromptResultVo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public Integer getScore() {
        return score;
    }

    public PePromptResultVo setScore(Integer score) {
        this.score = score;
        return this;
    }

    public Integer getToken() {
        return token;
    }

    public PePromptResultVo setToken(Integer token) {
        this.token = token;
        return this;
    }

    public Integer getCorrectNum() {
        return correctNum;
    }

    public PePromptResultVo setCorrectNum(Integer correctNum) {
        this.correctNum = correctNum;
        return this;
    }

    public Integer getErrorNum() {
        return errorNum;
    }

    public PePromptResultVo setErrorNum(Integer errorNum) {
        this.errorNum = errorNum;
        return this;
    }

    public Double getTime() {
        return time;
    }

    public PePromptResultVo setTime(Double time) {
        this.time = time;
        return this;
    }

    public FileInfoVo getFileInfo() {
        return fileInfo;
    }

    public PePromptResultVo setFileInfo(FileInfoVo fileInfo) {
        this.fileInfo = fileInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PePromptResultVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    manualScore: ").append(toIndentedString(manualScore)).append("\n");
        sb.append("    modelConfig: ").append(toIndentedString(modelConfig)).append("\n");
        sb.append("    variableVo: ").append(toIndentedString(variableVo)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    token: ").append(toIndentedString(token)).append("\n");
        sb.append("    correctNum: ").append(toIndentedString(correctNum)).append("\n");
        sb.append("    errorNum: ").append(toIndentedString(errorNum)).append("\n");
        sb.append("    time: ").append(toIndentedString(time)).append("\n");
        sb.append("    fileInfo: ").append(toIndentedString(fileInfo)).append("\n");
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
        PePromptResultVo pePromptResultVo = (PePromptResultVo) o;
        return Objects.equals(this.id, pePromptResultVo.id) && Objects.equals(this.name, pePromptResultVo.name)
            && Objects.equals(this.content, pePromptResultVo.content) && Objects.equals(this.model,
            pePromptResultVo.model) && Objects.equals(this.manualScore, pePromptResultVo.manualScore) && Objects.equals(
            this.modelConfig, pePromptResultVo.modelConfig) && Objects.equals(this.variableVo,
            pePromptResultVo.variableVo) && Objects.equals(this.creator, pePromptResultVo.creator) && Objects.equals(
            this.updater, pePromptResultVo.updater) && Objects.equals(this.createdOn, pePromptResultVo.createdOn)
            && Objects.equals(this.updatedOn, pePromptResultVo.updatedOn) && Objects.equals(this.score,
            pePromptResultVo.score) && Objects.equals(this.token, pePromptResultVo.token) && Objects.equals(
            this.correctNum, pePromptResultVo.correctNum) && Objects.equals(this.errorNum, pePromptResultVo.errorNum)
            && Objects.equals(this.time, pePromptResultVo.time) && Objects.equals(this.fileInfo,
            pePromptResultVo.fileInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content, model, manualScore, modelConfig, variableVo, creator, updater, createdOn,
            updatedOn, score, token, correctNum, errorNum, time, fileInfo);
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
