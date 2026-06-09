/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ListFaqFileReq
 */

@Validated

public class ListFaqFileReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("file_name")
    private String fileName = null;

    @JsonProperty("file_type")
    private String fileType = null;

    @JsonProperty("file_status")
    private String fileStatus = null;

    @JsonProperty("page_num")
    private Integer pageNum = null;

    @JsonProperty("page_size")
    private Integer pageSize = null;

    @JsonProperty("ids")
    @Valid
    @Size(min = 1, max = 1000)
    private List<@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Length(min = 1, max = 64) String> ids = null;

    public String getFileName() {
        return fileName;
    }

    public ListFaqFileReq setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public ListFaqFileReq setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public ListFaqFileReq setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
        return this;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public ListFaqFileReq setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public ListFaqFileReq setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public List<@Size(min = 1, max = 1000) String> getIds() {
        return ids;
    }

    public ListFaqFileReq setIds(List<@Size(min = 1, max = 1000) String> ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListFaqFileReq {\n");

        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    fileType: ").append(toIndentedString(fileType)).append("\n");
        sb.append("    fileStatus: ").append(toIndentedString(fileStatus)).append("\n");
        sb.append("    pageNum: ").append(toIndentedString(pageNum)).append("\n");
        sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
        sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
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
        ListFaqFileReq listFaqFileReq = (ListFaqFileReq) o;
        return Objects.equals(this.fileName, listFaqFileReq.fileName) && Objects.equals(this.fileType,
            listFaqFileReq.fileType) && Objects.equals(this.fileStatus, listFaqFileReq.fileStatus) && Objects.equals(
            this.pageNum, listFaqFileReq.pageNum) && Objects.equals(this.pageSize, listFaqFileReq.pageSize)
            && Objects.equals(this.ids, listFaqFileReq.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileType, fileStatus, pageNum, pageSize, ids);
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
