/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper.workspace;

import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkspaceMemberMapper {

    List<WorkSpaceMemberEntity> selectWorkspaceMemberListByCondition(@Param("workspaceId") String workspaceId,
        @Param("memberName") String memberName, @Param("role") String role);

    void insertWorkspaceMember(@Param("workspaceMember") WorkSpaceMemberEntity workSpaceMember);

    int batchInsertWorkspaceMember(
        @Param("workspaceMemberInfoList") List<WorkSpaceMemberEntity> workspaceMemberInfoList);

    int batchDeleteWorkspaceMemberByIds(@Param("workspaceId") String workspaceId,
        @Param("memberIdList") List<String> memberIdList, @Param("updater") String updater,
        @Param("updateId") String updateId);

    int batchUpdateWorkspaceMemberRole(
        @Param("workspaceMemberInfoList") List<WorkSpaceMemberEntity> workspaceMemberInfoList);

    WorkSpaceMemberEntity selectByMemberIdAndWorkspaceId(@Param("memberId") String memberId,
        @Param("workspaceId") String workspaceId);

    void deleteWorkspaceMemberByMemberIdWorkspaceId(@Param("workspaceMember") WorkSpaceMemberEntity workSpaceMember);

    List<WorkSpaceMemberEntity> selectAllWorkspaceByMemberId(@Param("memberId") String memberId);

    int countMemberByRoleAndWorkspaceId(@Param("workspaceId") String workspaceId, @Param("role") String role);

    int updateMemberRole(@Param("member") WorkSpaceMemberEntity workSpaceMember);

    int countMemberByWorkspaceIdAndDomainId(@Param("workspaceId") String workspaceId, @Param("domainId") String domainId);
}
