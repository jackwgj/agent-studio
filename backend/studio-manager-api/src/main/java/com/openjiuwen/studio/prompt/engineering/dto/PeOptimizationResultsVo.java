/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This is the information of the Optimization Task Results
 */
@ApiModel(description = "This is the information of the Optimization Task Results")

@Validated
public class PeOptimizationResultsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("total")
    private Integer total = null;

    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("dataset_name")
    private String datasetName = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("best_prompt")
    private String bestPrompt = null;

    @JsonProperty("error_msg")
    private String errorMsg = null;

    @JsonProperty("progress_rate")
    private Double progressRate = null;

    @JsonProperty("num_iter")
    private String numIter = null;

    @JsonProperty("prompts")
    @Valid
    private List<PeOptimizationResultVo> prompts = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    public Integer getTotal() {
        return total;
    }

    public PeOptimizationResultsVo setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public PeOptimizationResultsVo setCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getName() {
        return name;
    }

    public PeOptimizationResultsVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PeOptimizationResultsVo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public PeOptimizationResultsVo setDatasetName(String datasetName) {
        this.datasetName = datasetName;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PeOptimizationResultsVo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getBestPrompt() {
        return bestPrompt;
    }

    public PeOptimizationResultsVo setBestPrompt(String bestPrompt) {
        this.bestPrompt = bestPrompt;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public PeOptimizationResultsVo setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public Double getProgressRate() {
        return progressRate;
    }

    public PeOptimizationResultsVo setProgressRate(Double progressRate) {
        this.progressRate = progressRate;
        return this;
    }

    public String getNumIter() {
        return numIter;
    }

    public PeOptimizationResultsVo setNumIter(String numIter) {
        this.numIter = numIter;
        return this;
    }

    public List<PeOptimizationResultVo> getPrompts() {
        return prompts;
    }

    public PeOptimizationResultsVo setPrompts(List<PeOptimizationResultVo> prompts) {
        this.prompts = prompts;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public PeOptimizationResultsVo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public PeOptimizationResultsVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeOptimizationResultsVo {\n");
        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    datasetName: ").append(toIndentedString(datasetName)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    bestPrompt: ").append(toIndentedString(bestPrompt)).append("\n");
        sb.append("    errorMsg: ").append(toIndentedString(errorMsg)).append("\n");
        sb.append("    progressRate: ").append(toIndentedString(progressRate)).append("\n");
        sb.append("    numIter: ").append(toIndentedString(numIter)).append("\n");
        sb.append("    prompts: ").append(toIndentedString(prompts)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeOptimizationResultsVo peOptimizationResultsVo = (PeOptimizationResultsVo) o;
        return Objects.equals(this.total, peOptimizationResultsVo.total) && Objects.equals(this.count,
            peOptimizationResultsVo.count) && Objects.equals(this.name, peOptimizationResultsVo.name) && Objects.equals(
            this.description, peOptimizationResultsVo.description) && Objects.equals(this.datasetName,
            peOptimizationResultsVo.datasetName) && Objects.equals(this.status, peOptimizationResultsVo.status)
            && Objects.equals(this.bestPrompt, peOptimizationResultsVo.bestPrompt) && Objects.equals(this.errorMsg,
            peOptimizationResultsVo.errorMsg) && Objects.equals(this.progressRate, peOptimizationResultsVo.progressRate)
            && Objects.equals(this.numIter, peOptimizationResultsVo.numIter) && Objects.equals(this.prompts,
            peOptimizationResultsVo.prompts) && Objects.equals(this.creator, peOptimizationResultsVo.creator)
            && Objects.equals(this.createdOn, peOptimizationResultsVo.createdOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, count, name, description, datasetName, status, bestPrompt, errorMsg, progressRate,
            numIter, prompts, creator, createdOn);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
