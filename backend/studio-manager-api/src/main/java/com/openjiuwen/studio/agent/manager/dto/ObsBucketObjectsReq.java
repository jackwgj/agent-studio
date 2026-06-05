/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ObsBucketObjectsReq
 */

@Validated

public class ObsBucketObjectsReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("bucketName")
    @Length(max = 100)
    private String bucketName = null;

    @JsonProperty("dir")
    @Length(max = 1000)
    private String dir = null;

    public String getBucketName() {
        return bucketName;
    }

    public ObsBucketObjectsReq setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getDir() {
        return dir;
    }

    public ObsBucketObjectsReq setDir(String dir) {
        this.dir = dir;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ObsBucketObjectsReq {\n");

        sb.append("    bucketName: ").append(toIndentedString(bucketName)).append("\n");
        sb.append("    dir: ").append(toIndentedString(dir)).append("\n");
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
        ObsBucketObjectsReq obsBucketObjectsReq = (ObsBucketObjectsReq) o;
        return Objects.equals(this.bucketName, obsBucketObjectsReq.bucketName) && Objects.equals(this.dir,
            obsBucketObjectsReq.dir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName, dir);
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
