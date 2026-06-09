/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * OcrRecognizeRsp
 */

@Validated

public class OcrRecognizeRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    @Valid
    @Size()
    private List<OcrResultInfo> data = null;

    public List<OcrResultInfo> getData() {
        return data;
    }

    public OcrRecognizeRsp setData(List<OcrResultInfo> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OcrRecognizeRsp {\n");

        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
        OcrRecognizeRsp ocrRecognizeRsp = (OcrRecognizeRsp) o;
        return Objects.equals(this.data, ocrRecognizeRsp.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
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
