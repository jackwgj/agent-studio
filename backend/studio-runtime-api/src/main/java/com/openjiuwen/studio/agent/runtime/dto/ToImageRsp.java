/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * ToImageRsp
 */

@Validated

public class ToImageRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    private Boolean success = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("mimetype")
    private String mimetype = null;

    @JsonProperty("filesize")
    private Long filesize = null;

    @JsonProperty("path")
    private String path = null;

    @JsonProperty("image")
    private byte[] image = null;

    public ToImageRsp setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ToImageRsp setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ToImageRsp setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getMimetype() {
        return mimetype;
    }

    public ToImageRsp setMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public Long getFilesize() {
        return filesize;
    }

    public ToImageRsp setFilesize(Long filesize) {
        this.filesize = filesize;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ToImageRsp setPath(String path) {
        this.path = path;
        return this;
    }

    public byte[] getImage() {
        return image;
    }

    public ToImageRsp setImage(byte[] image) {
        this.image = image;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ToImageRsp {\n");

        sb.append("    success: ").append(toIndentedString(success)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    mimetype: ").append(toIndentedString(mimetype)).append("\n");
        sb.append("    filesize: ").append(toIndentedString(filesize)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    image: ").append(toIndentedString(image)).append("\n");
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
        ToImageRsp toImageRsp = (ToImageRsp) o;
        return Objects.equals(this.success, toImageRsp.success) && Objects.equals(this.message, toImageRsp.message)
            && Objects.equals(this.url, toImageRsp.url) && Objects.equals(this.mimetype, toImageRsp.mimetype)
            && Objects.equals(this.filesize, toImageRsp.filesize) && Objects.equals(this.path, toImageRsp.path)
            && Arrays.equals(this.image, toImageRsp.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, url, mimetype, filesize, path, image);
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
