package com.openjiuwen.studio.agent.space.common.model.sysparam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuerySysParamsByIdsReq {

    private List<String> ids;

    public List<String> getIds() {
        return ids == null ? null : new ArrayList<>(ids);
    }

    public void setIds(List<String> ids) {
        this.ids = ids == null ? null : new ArrayList<>(ids);
    }
}
