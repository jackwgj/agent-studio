/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ResolveAudioFileReq
 */

@Validated

public class ResolveAudioFileReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("urls")
    @Valid
    @NotNull
    @Size()
    private List<@Length() String> urls = new ArrayList<String>();

    @JsonProperty("add_punc")
    private Boolean addPunc = null;

    @JsonProperty("digit_norm")
    private Boolean digitNorm = null;

    public List<@Size(min = 1, max = 2000) String> getUrls() {
        return urls;
    }

    public ResolveAudioFileReq setUrls(List<@Size(min = 1, max = 2000) String> urls) {
        this.urls = urls;
        return this;
    }

    public ResolveAudioFileReq setAddPunc(Boolean addPunc) {
        this.addPunc = addPunc;
        return this;
    }

    public Boolean isAddPunc() {
        return addPunc;
    }

    public ResolveAudioFileReq setDigitNorm(Boolean digitNorm) {
        this.digitNorm = digitNorm;
        return this;
    }

    public Boolean isDigitNorm() {
        return digitNorm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolveAudioFileReq {\n");

        sb.append("    urls: ").append(toIndentedString(urls)).append("\n");
        sb.append("    addPunc: ").append(toIndentedString(addPunc)).append("\n");
        sb.append("    digitNorm: ").append(toIndentedString(digitNorm)).append("\n");
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
        ResolveAudioFileReq resolveAudioFileReq = (ResolveAudioFileReq) o;
        return Objects.equals(this.urls, resolveAudioFileReq.urls) && Objects.equals(this.addPunc,
            resolveAudioFileReq.addPunc) && Objects.equals(this.digitNorm, resolveAudioFileReq.digitNorm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urls, addPunc, digitNorm);
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
