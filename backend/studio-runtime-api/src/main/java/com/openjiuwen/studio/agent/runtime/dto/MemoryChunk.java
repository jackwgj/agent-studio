/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 记忆片段
 */
@ApiModel(description = "记忆片段")

@Validated

public class MemoryChunk implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Integer id = null;

    @JsonProperty("start")
    private Long start = null;

    @JsonProperty("end")
    private Long end = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("summary")
    private String summary = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    public Integer getId() {
        return id;
    }

    public MemoryChunk setId(Integer id) {
        this.id = id;
        return this;
    }

    public Long getStart() {
        return start;
    }

    public MemoryChunk setStart(Long start) {
        this.start = start;
        return this;
    }

    public Long getEnd() {
        return end;
    }

    public MemoryChunk setEnd(Long end) {
        this.end = end;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public MemoryChunk setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public MemoryChunk setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public MemoryChunk setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public MemoryChunk setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MemoryChunk {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
        sb.append("    end: ").append(toIndentedString(end)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
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
        MemoryChunk memoryChunk = (MemoryChunk) o;
        return Objects.equals(this.id, memoryChunk.id) && Objects.equals(this.start, memoryChunk.start)
            && Objects.equals(this.end, memoryChunk.end) && Objects.equals(this.offset, memoryChunk.offset)
            && Objects.equals(this.limit, memoryChunk.limit) && Objects.equals(this.summary, memoryChunk.summary)
            && Objects.equals(this.createTime, memoryChunk.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, offset, limit, summary, createTime);
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
