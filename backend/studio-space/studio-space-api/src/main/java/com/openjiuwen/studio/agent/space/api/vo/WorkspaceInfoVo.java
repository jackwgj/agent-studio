package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
public class WorkspaceInfoVo {

    private String id;

    private String name;

    private String icon;

    private String description;

    private String status;

}
