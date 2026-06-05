/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ASR配置。
 */
@ApiModel(description = "ASR配置。")
@Validated
public class AsrConfig {
    @JsonProperty("audio_format")
    @NotBlank
    private String audioFormat = null;

    @JsonProperty("property")
    @NotBlank
    private String property = null;

    @JsonProperty("add_punc")
    private String addPunc = null;

    @JsonProperty("digit_norm")
    private String digitNorm = null;

    @JsonProperty("vocabulary_id")
    private String vocabularyId = null;

    @JsonProperty("need_word_info")
    private String needWordInfo = null;

    public String getAudioFormat() {
        return audioFormat;
    }

    public AsrConfig setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public AsrConfig setProperty(String property) {
        this.property = property;
        return this;
    }

    public String getAddPunc() {
        return addPunc;
    }

    public AsrConfig setAddPunc(String addPunc) {
        this.addPunc = addPunc;
        return this;
    }

    public String getDigitNorm() {
        return digitNorm;
    }

    public AsrConfig setDigitNorm(String digitNorm) {
        this.digitNorm = digitNorm;
        return this;
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public AsrConfig setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
        return this;
    }

    public String getNeedWordInfo() {
        return needWordInfo;
    }

    public AsrConfig setNeedWordInfo(String needWordInfo) {
        this.needWordInfo = needWordInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("class AsrConfig {\n");

        builder.append("    audioFormat: ").append(toIndentedString(audioFormat)).append("\n");
        builder.append("    property: ").append(toIndentedString(property)).append("\n");
        builder.append("    addPunc: ").append(toIndentedString(addPunc)).append("\n");
        builder.append("    digitNorm: ").append(toIndentedString(digitNorm)).append("\n");
        builder.append("    vocabularyId: ").append(toIndentedString(vocabularyId)).append("\n");
        builder.append("    needWordInfo: ").append(toIndentedString(needWordInfo)).append("\n");
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AsrConfig asrConfig = (AsrConfig) obj;
        return Objects.equals(this.audioFormat, asrConfig.audioFormat) && Objects.equals(this.property,
            asrConfig.property) && Objects.equals(this.addPunc, asrConfig.addPunc) && Objects.equals(this.digitNorm,
            asrConfig.digitNorm) && Objects.equals(this.vocabularyId, asrConfig.vocabularyId) && Objects.equals(
            this.needWordInfo, asrConfig.needWordInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audioFormat, property, addPunc, digitNorm, vocabularyId, needWordInfo);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
