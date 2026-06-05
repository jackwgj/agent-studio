package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.openjiuwen.studio.agent.space.api.vo.WorkspaceInfoVo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
public class WorkspaceInfoResp {
    private Integer count;

    private List<WorkspaceInfoVo> workspaceList;
}
