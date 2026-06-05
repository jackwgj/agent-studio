/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
public class WorkSpaceMemberEntity {

    private String id;

    private String workspaceId;

    private String memberId;

    private String memberName;

    private String memberSource;

    private String domainId;

    private String role;

    private Integer status;

    private String creator;

    private String creatorId;

    private String updater;

    private String updaterId;

    private Date createdOn;

    private Date updatedOn;

}
