package com.openjiuwen.studio.agent.space.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfoVo implements Cloneable {

    private int pageSize;

    private int curPage;

    private long total;

    @Override
    public PageInfoVo clone() {
        try {
            PageInfoVo obj = (PageInfoVo) super.clone();
            obj.setPageSize(pageSize);
            obj.setCurPage(curPage);
            obj.setTotal(total);
            return obj;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
