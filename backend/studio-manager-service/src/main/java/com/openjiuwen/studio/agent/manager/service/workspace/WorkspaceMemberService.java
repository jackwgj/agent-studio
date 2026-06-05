/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service.workspace;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.openjiuwen.studio.agent.common.annotation.DistributedLock;
import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AddWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.CommonResponse;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberListRsp;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberRoleRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberOwnershipBody;
import com.openjiuwen.studio.agent.manager.dto.MemberRole;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceMemberListQo;
import com.openjiuwen.studio.agent.manager.dto.RoleInfo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody1;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberInfo;
import com.openjiuwen.studio.agent.manager.dto.iam.GetIamUserResponse;
import com.openjiuwen.studio.agent.manager.entity.WorkSpaceMemberEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMemberMapper;
import com.openjiuwen.studio.agent.manager.service.IWorkSpaceMemberService;
import com.openjiuwen.studio.agent.manager.service.SkuManageService;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class WorkspaceMemberService implements IWorkSpaceMemberService {

    @Resource
    private WorkspaceMemberMapper workspaceMemberMapper;

    @Resource
    private WorkspaceMapper workspaceMapper;

    @Resource
    private SkuManageService skuManageService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    IamServiceUtils iamServiceUtils;

    @Value("${workspace.role:}")
    private String roleListStr;

    @Autowired
    private I18nUtil i18nUtil;

    @Override
    @Transactional
    @OperationLog
    @DistributedLock(prefix = "workspace-member:add:")
    public Integer batchAddWorkspaceMember(String projectId, String workspaceId, WorkspaceMemberBody body) {
        if (CollectionUtils.isEmpty(body.getMembers())) {
            return 0;
        }

        skuManageService.validateExceededLimitOrNot(CommonConstant.SKU_ATTR_CODE.TEAM_SPACE_MEMBER_COUNT,
            workspaceMemberMapper.countMemberByWorkspaceIdAndDomainId(RequestContextUtils.getRequestWorkspaceId(),
                RequestContextUtils.getRequestUserDomainId()) + body.getMembers().size() - 1);

        ArrayList<WorkSpaceMemberEntity> workSpaceMemberEntityList = new ArrayList<>();
        Date currentDate = new Date();
        String currentUserId = RequestContextUtils.getRequestUserId();
        String userName = RequestContextUtils.getRequestUserName();

        checkMemberOwnerOrAdminPermission(projectId, workspaceId, currentUserId);

        HashSet<String> memberIdSet = new HashSet<>();
        for (AddWorkspaceMemberReq member : body.getMembers()) {
            WorkSpaceMemberEntity existedMember =
                workspaceMemberMapper.selectByMemberIdAndWorkspaceId(member.getMemberId(), workspaceId);
            if (existedMember != null) {
                throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_ALREADY_EXISTED, member.getMemberName());
            }

            if (MemberRole.OWNER.getValue().equals(member.getRole().getValue())) {
                throw new AgentStudioException(StudioError.INVALID_ROLE);
            }

            if (memberIdSet.contains(member.getMemberId())) {
                throw new AgentStudioException(StudioError.REPEAT_MEMBER);
            }

            WorkSpaceMemberEntity workSpaceMemberEntity = new WorkSpaceMemberEntity();
            BeanUtils.copyProperties(member, workSpaceMemberEntity);
            workSpaceMemberEntity.setId(UUID.randomUUID().toString())
                .setWorkspaceId(workspaceId)
                .setDomainId(RequestContextUtils.getRequestUserDomainId())
                .setRole(member.getRole().getValue())
                .setCreator(userName)
                .setCreatorId(currentUserId)
                .setUpdater(userName)
                .setUpdaterId(currentUserId)
                .setUpdatedOn(currentDate)
                .setCreatedOn(currentDate);
            workSpaceMemberEntityList.add(workSpaceMemberEntity);
            memberIdSet.add(member.getMemberId());
        }

        return workspaceMemberMapper.batchInsertWorkspaceMember(workSpaceMemberEntityList);
    }

    @Override
    @OperationLog
    public Integer batchDeleteWorkspaceMember(String projectId, String workspaceId, DeleteWorkspaceMemberReq body) {
        String requestUserId = RequestContextUtils.getRequestUserId();
        WorkSpaceMemberEntity currentMember = checkMemberOwnerOrAdminPermission(projectId, workspaceId, requestUserId);

        if (CollectionUtils.isEmpty(body.getUserIds())) {
            return 0;
        }

        for (String memberId : body.getUserIds()) {
            WorkSpaceMemberEntity existedMember =
                workspaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId);

            if (existedMember == null) {
                log.error("The member {} not exist in current workspace.", memberId);
                throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_NOT_EXIST, memberId);
            }

            // 空间管理员不能删除空间所有者
            if (MemberRole.ADMIN.getValue().equals(currentMember.getRole())
                && MemberRole.OWNER.getValue().equals(existedMember.getRole())) {
                log.error("the current user: {} doesn't have permission to operate this in current workspace {}",
                    requestUserId, workspaceId);
                throw new AgentStudioException(StudioError.USER_NO_PERMISSION_DO_THIS);
            }

            // 空间管理员不能删除自己
            if (MemberRole.ADMIN.getValue().equals(currentMember.getRole()) && currentMember.getMemberId().equals(memberId)) {
                log.error("the current user: {} doesn't have permission to operate this in current workspace {}",
                        requestUserId, workspaceId);
                throw new AgentStudioException(StudioError.USER_NO_PERMISSION_DO_THIS);
            }

            if (MemberRole.OWNER.getValue().equals(existedMember.getRole())) {
                log.error("owner not support delete");
                throw new AgentStudioException(StudioError.USER_NO_PERMISSION_DO_THIS);
            }
        }

        return workspaceMemberMapper.batchDeleteWorkspaceMemberByIds(workspaceId, body.getUserIds(),
            RequestContextUtils.getRequestUserName(), requestUserId);
    }

    @Override
    @OperationLog
    public Integer batchUpdateWorkspaceMemberRole(String workspaceId, String projectId, WorkspaceMemberBody1 body) {
        if (CollectionUtils.isEmpty(body.getMembers())) {
            return 0;
        }
        WorkSpaceMemberEntity currentMember =
            checkMemberOwnerOrAdminPermission(projectId, workspaceId, RequestContextUtils.getRequestUserId());

        HashSet<String> memberIdSet = new HashSet<>();
        ArrayList<WorkSpaceMemberEntity> workspaceMemberInfoList = new ArrayList<>();
        for (UpdateWorkspaceMemberReq updateMemberInfo : body.getMembers()) {
            if (Strings.CS.equals(updateMemberInfo.getMemberId(), RequestContextUtils.getRequestUserId())) {
                log.info("the user can not modify self role!");
                throw new AgentStudioException(StudioError.USER_WORKSPACE_PERMISSION_INVALID);
            }
            WorkSpaceMemberEntity workSpaceMemberEntity =
                workspaceMemberMapper.selectByMemberIdAndWorkspaceId(updateMemberInfo.getMemberId(), workspaceId);
            if (workSpaceMemberEntity == null) {
                throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_NOT_EXIST, updateMemberInfo.getMemberId());
            }

            // 空间管理员不能修改空间所有者角色
            if (MemberRole.ADMIN.getValue().equals(currentMember.getRole())
                && MemberRole.OWNER.getValue().equals(workSpaceMemberEntity.getRole())) {
                log.error("the current user {} doesn't have permission to operate this in current workspace {}",
                    RequestContextUtils.getRequestUserId(), workspaceId);
                throw new AgentStudioException(StudioError.USER_NO_PERMISSION_DO_THIS);
            }

            if (MemberRole.OWNER.getValue().equals(updateMemberInfo.getRole().getValue())) {
                throw new AgentStudioException(StudioError.INVALID_ROLE);
            }

            if (memberIdSet.contains(updateMemberInfo.getMemberId())) {
                throw new AgentStudioException(StudioError.REPEAT_MEMBER);
            }

            workSpaceMemberEntity.setUpdater(RequestContextUtils.getRequestUserName());
            workSpaceMemberEntity.setUpdaterId(RequestContextUtils.getRequestUserId());
            workSpaceMemberEntity.setRole(updateMemberInfo.getRole().getValue());
            workspaceMemberInfoList.add(workSpaceMemberEntity);
            memberIdSet.add(updateMemberInfo.getMemberId());
        }
        return workspaceMemberMapper.batchUpdateWorkspaceMemberRole(workspaceMemberInfoList);
    }

    @Override
    public GetWorkspaceMemberListRsp queryIamUserList(String projectId) {
        String domainToken = iamServiceUtils.queryDomainTokenFromCache("");

        GetIamUserResponse response = iamServiceUtils.queryIamUserList(domainToken);

        GetWorkspaceMemberListRsp memberListRsp = new GetWorkspaceMemberListRsp();

        if (CollectionUtils.isEmpty(response.getUsers())) {
            return memberListRsp;
        }

        List<WorkspaceMemberInfo> workspaceMemberInfoList = response.getUsers().stream().map(iamUser -> {
            WorkspaceMemberInfo workspaceMemberInfo = new WorkspaceMemberInfo();
            workspaceMemberInfo.setMemberName(iamUser.getName());
            workspaceMemberInfo.setMemberId(iamUser.getId());
            return workspaceMemberInfo;
        }).collect(Collectors.toCollection(ArrayList::new));

        memberListRsp.setWorkspaceList(workspaceMemberInfoList);
        memberListRsp.setCount(workspaceMemberInfoList.size());
        return memberListRsp;
    }

    @Override
    public GetWorkspaceMemberRoleRsp queryRoleList(String projectId) {
        GetWorkspaceMemberRoleRsp resp = new GetWorkspaceMemberRoleRsp();

        if (StringUtils.isBlank(roleListStr)) {
            return resp;
        }
        List<RoleInfo> roleInfoList;

        try {
            roleInfoList = objectMapper.readValue(roleListStr, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("parse role list failed, error: {}", e.getMessage());
            return resp;
        }
        resp.setRoleList(roleInfoList);
        return resp;
    }

    public WorkspaceMemberInfo queryWorkspaceMemberDetail(String projectId, String userId, String workspaceId) {
        WorkspaceMemberInfo workspaceMemberInfo = new WorkspaceMemberInfo();
        WorkSpaceMemberEntity workSpaceMemberEntity =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(userId, workspaceId);

        if (workSpaceMemberEntity == null) {
            return workspaceMemberInfo;
        }

        BeanUtils.copyProperties(workSpaceMemberEntity, workspaceMemberInfo);
        return workspaceMemberInfo;
    }

    @Override
    public GetWorkspaceMemberListRsp queryWorkspaceMemberList(String projectId, QueryWorkspaceMemberListQo param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<WorkSpaceMemberEntity> workSpaceMemberEntityList = workspaceMemberMapper
            .selectWorkspaceMemberListByCondition(param.getWorkspaceId(), param.getMemberName(), param.getRole());

        List<WorkspaceMemberInfo> workspaceMemberInfoList = workSpaceMemberEntityList.stream().map((workSpaceMember -> {
            WorkspaceMemberInfo workspaceMemberInfo = new WorkspaceMemberInfo();
            BeanUtils.copyProperties(workSpaceMember, workspaceMemberInfo);
            return workspaceMemberInfo;
        })).toList();

        GetWorkspaceMemberListRsp getWorkspaceMemberListRsp = new GetWorkspaceMemberListRsp();
        getWorkspaceMemberListRsp.setCount(
            workspaceMemberMapper.countMemberByRoleAndWorkspaceId(param.getWorkspaceId(), param.getRole()));
        getWorkspaceMemberListRsp.setWorkspaceList(workspaceMemberInfoList);
        return getWorkspaceMemberListRsp;
    }

    @Override
    @Transactional
    public CommonResponse transferWorkspaceOwnership(String projectId, MemberOwnershipBody body) {
        CommonResponse response = new CommonResponse();
        WorkSpaceMemberEntity ownerInfo = workspaceMemberMapper
            .selectByMemberIdAndWorkspaceId(RequestContextUtils.getRequestUserId(), body.getWorkspaceId());

        if (ownerInfo == null || !MemberRole.OWNER.getValue().equals(ownerInfo.getRole())) {
            throw new AgentStudioException(StudioError.INVALID_ROLE);
        }

        ownerInfo.setRole(MemberRole.ADMIN.getValue());

        WorkSpaceMemberEntity workSpaceMemberEntity =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(body.getNextOwnerId(), body.getWorkspaceId());

        if (workSpaceMemberEntity == null) {
            throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_NOT_EXIST, body.getNextOwnerId());
        }

        workSpaceMemberEntity.setRole(MemberRole.OWNER.getValue());
        workSpaceMemberEntity.setUpdater(RequestContextUtils.getRequestUserName());
        workSpaceMemberEntity.setUpdaterId(RequestContextUtils.getRequestUserId());

        workspaceMemberMapper.updateMemberRole(ownerInfo);
        workspaceMemberMapper.updateMemberRole(workSpaceMemberEntity);

        response.setCode(200);
        response.setData(body.getNextOwnerId());
        return response;
    }

    @Override
    public CommonResponse exitWorkspace(String projectId, String workspaceId) {
        CommonResponse response = new CommonResponse();

        WorkspaceInfo workspaceInfo = workspaceMapper.selectById(projectId, workspaceId);

        if (CommonConstant.WORKSPACE.TYPE.PERSON_TYPE.equals(workspaceInfo.getType())) {
            throw new AgentStudioException(StudioError.INVALID_WORKSPACE_TYPE);
        }

        WorkSpaceMemberEntity member =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(RequestContextUtils.getRequestUserId(), workspaceId);

        if (member == null) {
            throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_NOT_EXIST);
        }

        if (MemberRole.OWNER.getValue().equals(member.getRole())) {
            throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_CAN_NOT_EXIT);
        }

        member.setStatus(0);
        member.setUpdater(RequestContextUtils.getRequestUserName());
        member.setUpdaterId(RequestContextUtils.getRequestUserId());
        workspaceMemberMapper.deleteWorkspaceMemberByMemberIdWorkspaceId(member);
        response.setCode(200);
        response.setData(member);
        return response;
    }

    public void addWorkspaceMember(String workspaceId, String iamUserId, String role) {
        Date currentDate = new Date();
        String currentUserId = RequestContextUtils.getRequestUserId();
        String currentUserName = RequestContextUtils.getRequestUserName();
        String domainId = RequestContextUtils.getRequestUserDomainId();

        WorkSpaceMemberEntity workSpaceMember =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(iamUserId, workspaceId);

        if (workSpaceMember != null) {
            throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_ALREADY_EXISTED,
                workSpaceMember.getMemberName());
        }

        WorkSpaceMemberEntity insertWorkspaceEntity = new WorkSpaceMemberEntity();
        insertWorkspaceEntity.setId(UUID.randomUUID().toString())
            .setWorkspaceId(workspaceId)
            .setMemberId(iamUserId)
            .setMemberName(currentUserName)
            .setMemberSource("IAM")
            .setDomainId(domainId)
            .setStatus(1)
            .setRole(role)
            .setCreator(currentUserId)
            .setCreatorId(currentUserId)
            .setCreatedOn(currentDate)
            .setUpdatedOn(currentDate)
            .setUpdaterId(currentUserId)
            .setUpdater(currentUserName);

        workspaceMemberMapper.insertWorkspaceMember(insertWorkspaceEntity);
    }

    public void deleteWorkspaceMember(String workspaceId, String iamUserId) {
        Date currentDate = new Date();
        String currentUserId = RequestContextUtils.getRequestUserId();
        String currentUserName = RequestContextUtils.getRequestUserName();

        WorkSpaceMemberEntity workSpaceMemberEntity =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(iamUserId, workspaceId);

        if (workSpaceMemberEntity == null) {
            throw new AgentStudioException(StudioError.WORKSPACE_MEMBER_NOT_EXIST, iamUserId);
        }

        workSpaceMemberEntity.setStatus(0);
        workSpaceMemberEntity.setUpdater(currentUserName);
        workSpaceMemberEntity.setUpdaterId(currentUserId);
        workSpaceMemberEntity.setUpdatedOn(currentDate);

        workspaceMemberMapper.deleteWorkspaceMemberByMemberIdWorkspaceId(workSpaceMemberEntity);
    }

    public List<WorkSpaceMemberEntity> queryAllWorkSpaceInfoByIamUserId(String iamUserId) {
        return workspaceMemberMapper.selectAllWorkspaceByMemberId(iamUserId);
    }

    public WorkSpaceMemberEntity checkMemberOwnerOrAdminPermission(String projectId, String workspaceId,
        String memberId) {
        WorkspaceEntity workspaceInfo = workspaceMapper.getWorkspaceByWorkspaceId(projectId, workspaceId);

        if (workspaceInfo == null) {
            throw new AgentStudioException(StudioError.WORKSPACE_NOT_EXISTED);
        }

        if (!CommonConstant.WORKSPACE.TYPE.TEAM_TYPE.equals(workspaceInfo.getType())) {
            throw new AgentStudioException(StudioError.INVALID_WORKSPACE_TYPE);
        }

        WorkSpaceMemberEntity currentMemberInfo =
            workspaceMemberMapper.selectByMemberIdAndWorkspaceId(memberId, workspaceId);

        if (!MemberRole.OWNER.getValue().equals(currentMemberInfo.getRole())
            && !MemberRole.ADMIN.getValue().equals(currentMemberInfo.getRole())) {
            log.error("the member {} doesn't have permission to operate this in current workspace {}", memberId,
                workspaceId);
            throw new AgentStudioException(StudioError.USER_NO_PERMISSION_DO_THIS);
        }
        return currentMemberInfo;
    }
}
