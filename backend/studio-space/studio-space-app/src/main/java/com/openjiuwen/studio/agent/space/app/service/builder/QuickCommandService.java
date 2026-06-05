/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.builder;

import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderOperateQuickCommandReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentBuilderQuickCommandResp;
import com.openjiuwen.studio.agent.space.app.enums.QuickCommandOperationType;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.app.util.iam.IamRoleUtil;
import com.openjiuwen.studio.agent.space.common.constant.QuickCommandConstants;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.CollectionUtil;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;
import com.openjiuwen.studio.agent.space.dao.application.AgentMapper;
import com.openjiuwen.studio.agent.space.dao.application.QuickCommandMapper;
import com.openjiuwen.studio.agent.space.dao.entity.QuickCommandEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class QuickCommandService {
    @Resource
    private QuickCommandMapper quickCommandMapper;

    @Resource
    private AgentMapper agentMapper;

    /**
     * 根据agentId获取快捷指令列表
     *
     * @param agentId 智能应用id
     * @param active
     * @param key
     * @return 快捷指令列表
     */
    public List<AgentBuilderQuickCommandResp> getQuickCommandList(String agentId, String active, String key) {
        log.info("get quick command list, agentId: {}", agentId);
        List<QuickCommandEntity> commandEntities = quickCommandMapper.getCommandListByAgentId(agentId, active, key);
        if (commandEntities == null) {
            return Collections.emptyList();
        }
        List<AgentBuilderQuickCommandResp> agentBuilderQuickCommandList = new ArrayList<>();
        for (QuickCommandEntity commandEntity : commandEntities) {
            AgentBuilderQuickCommandResp AgentBuilderQuickCommandResp = getAgentBuilderQuickCommandResp(commandEntity);
            agentBuilderQuickCommandList.add(AgentBuilderQuickCommandResp);
        }
        log.info("get quick command list success, agentId: {}, command size: {}", agentId,
            agentBuilderQuickCommandList.size());
        return agentBuilderQuickCommandList;
    }

    private AgentBuilderQuickCommandResp getAgentBuilderQuickCommandResp(QuickCommandEntity commandEntity) {
        AgentBuilderQuickCommandResp AgentBuilderQuickCommandResp = new AgentBuilderQuickCommandResp();
        AgentBuilderQuickCommandResp.setCommandId(commandEntity.getId());
        AgentBuilderQuickCommandResp.setAgentId(commandEntity.getAgentId());
        AgentBuilderQuickCommandResp.setQuickCommandName(commandEntity.getName());
        AgentBuilderQuickCommandResp.setActive(commandEntity.getActive());
        AgentBuilderQuickCommandResp.setDescription(commandEntity.getDescription());
        AgentBuilderQuickCommandResp.setContent(commandEntity.getContent());
        AgentBuilderQuickCommandResp.setDisplayNo(commandEntity.getDisplayNo());
        AgentBuilderQuickCommandResp.setRecommend(commandEntity.getRecommend());
        AgentBuilderQuickCommandResp.setDomainId(commandEntity.getDomainId());
        return AgentBuilderQuickCommandResp;
    }

    /**
     * 操作快捷指令
     *
     * @param reqList 操作请求列表
     */
    @Transactional
    public void operateCommand(List<AgentBuilderOperateQuickCommandReq> reqList) {
        log.info("QuickCommandService.operateCommand start.");

        if (!IamRoleUtil.checkAdminRole()) {
            log.error("operateCommand failed. no permission.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.NO_OPERATION_PERMISSION);
        }

        List<AgentBuilderOperateQuickCommandReq> insertList = new ArrayList<>();
        List<AgentBuilderOperateQuickCommandReq> updateList = new ArrayList<>();
        List<String> deleteIdList = new ArrayList<>();
        List<AgentBuilderOperateQuickCommandReq> enableList = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();

        Integer displayNo = null;
        Set<String> existNameSet = new HashSet<>();
        for (AgentBuilderOperateQuickCommandReq req : reqList) {
            QuickCommandOperationType operate = QuickCommandOperationType.getByType(req.getOperateType());
            if (operate == null) {
                log.warn("unsupported operate type: {}", req.getOperateType());
                continue;
            }

            switch (operate) {
                case ADD -> {
                    // 参数校验
                    validateAddRequest(req, existNameSet);

                    // 设置默认值
                    req.setActive(req.getActive() == null ? QuickCommandConstants.ACTIVE_FALSE : req.getActive());
                    req.setRecommend(
                        req.getRecommend() == null ? QuickCommandConstants.RECOMMEND_FALSE : req.getRecommend());

                    // 设置排序号
                    displayNo = setDisplayNo(req, displayNo);

                    // 检查命令名称是否已存在
                    checkCommandNameExists(req);

                    // 检查Agent是否存在
                    checkAgentExists(req);

                    insertList.add(req);
                }
                case UPDATE -> {
                    commandExistCheck(processedIds, req);
                    updateList.add(req);
                }
                case DELETE -> {
                    commandExistCheck(processedIds, req);
                    deleteIdList.add(req.getCommandId());
                }
                case ENABLE -> {
                    commandExistCheck(processedIds, req);
                    enableList.add(req);
                }
                default -> log.warn("unsupported operate type: {}", req.getOperateType());
            }
        }

        batchInsert(insertList);
        batchUpdate(updateList);
        batchDelete(deleteIdList);
        batchUpdateEnable(enableList);
        log.info("QuickCommandService.operateCommand end.");
    }

    /**
     * 验证添加请求的有效性。
     *
     * @param req 包含快速命令相关信息的请求对象
     * @param existNameSet 已存在的快速命令名称集合，用于检查名称是否重复
     * @throws AgentSpaceException 如果请求中的快速命令名称、内容或Agent ID为空，或者快速命令名称已存在
     */
    private void validateAddRequest(AgentBuilderOperateQuickCommandReq req, Set<String> existNameSet) {
        // 检查快速命令名称是否为空
        if (StringUtil.isEmpty(req.getQuickCommandName())) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_NAME_NOT_EXIST);
        }
        // 检查快速命令内容是否为空
        if (StringUtil.isEmpty(req.getContent())) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_CONTENT_NOT_EXIST);
        }
        // 检查Agent ID是否为空
        if (StringUtil.isEmpty(req.getAgentId())) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_ID_NOT_EXIST);
        }

        // 检查快速命令名称是否已存在
        if (existNameSet.contains(req.getQuickCommandName())) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_NAME_DUPLICATE, req.getQuickCommandName());
        } else {
            // 如果名称不存在于集合中，则将其添加到集合中
            existNameSet.add(req.getQuickCommandName());
        }
    }

    /**
     * 设置显示编号。如果请求中的显示编号为空，则根据传入的displayNo参数或数据库中的最大显示编号来确定新的显示编号。
     *
     * @param req 包含操作快速命令请求信息的对象
     * @param displayNo 当前的显示编号，可能为空
     * @return 返回设置后的显示编号
     */
    private Integer setDisplayNo(AgentBuilderOperateQuickCommandReq req, Integer displayNo) {
        // 检查请求中的显示编号是否为空
        if (req.getDisplayNo() == null) {
            // 如果传入的displayNo参数为空
            if (displayNo == null) {
                // 从数据库中获取该代理ID下的所有命令，按显示编号降序排列
                List<QuickCommandEntity> commandDisplayNoDesc = quickCommandMapper.getCommandByAgentIdAndDisplayNoDesc(
                    req.getAgentId());
                // 如果列表不为空，则新的显示编号为列表中第一个元素的显示编号加1，否则为1
                displayNo = CollectionUtil.isNotEmpty(commandDisplayNoDesc) ? commandDisplayNoDesc.get(0).getDisplayNo()
                    + 1 : 1;
            } else {
                // 如果displayNo不为空，则新的显示编号为displayNo加1
                displayNo = displayNo + 1;
            }
            // 将新的显示编号设置到请求对象中
            req.setDisplayNo(displayNo);
        }
        // 返回设置后的显示编号
        return displayNo;
    }

    private void checkCommandNameExists(AgentBuilderOperateQuickCommandReq req) {
        if (quickCommandMapper.getCommandByAgentIdAndName(req.getQuickCommandName(), req.getAgentId()) != null) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_NAME_DUPLICATE, req.getQuickCommandName());
        }
    }

    private void checkAgentExists(AgentBuilderOperateQuickCommandReq req) {
        if (agentMapper.queryAgentById(req.getAgentId()) == null) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_NOT_EXIST, req.getAgentId());
        }
    }

    /**
     * 校验快捷指令是否存在并且是否被重复执行
     *
     * @param processedIds 已处理的指令ID集合，用于检测重复执行
     * @param req 包含快捷指令信息的请求对象
     * @throws AgentSpaceException 当指令ID为空、指令不存在或指令重复时抛出异常
     */
    private void commandExistCheck(Set<String> processedIds, AgentBuilderOperateQuickCommandReq req) {
        // 检查指令ID是否为空
        if (req.getCommandId() == null) {
            log.error("commandId is null");
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_ID_NOT_EXIST);
        }
        // 通过指令ID查询指令是否存在
        QuickCommandEntity existCheck = quickCommandMapper.getCommandById(req.getCommandId());
        // 如果指令不存在，抛出异常
        if (existCheck == null) {
            log.error("quick command not exist, commonId: {}", req.getCommandId());
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_NOT_EXIST, req.getCommandId());
        }
        // 检查指令ID是否已经在已处理集合中，避免重复执行
        if (processedIds.contains(req.getCommandId())) {
            log.error("duplicate operation exists in the Id: {}.", req.getCommandId());
            throw new AgentSpaceException(AgentSpaceErrorCodes.QUICK_COMMAND_OPERATION_DUPLICATE, req.getCommandId());
        }
        // 将指令ID添加到已处理集合中
        processedIds.add(req.getCommandId());
    }

    private void batchDelete(List<String> deleteIdList) {
        if (CollectionUtil.isEmpty(deleteIdList)) {
            return;
        }
        log.info("batchDelete start, size: {}", deleteIdList.size());
        quickCommandMapper.deleteBatchIds(deleteIdList);
    }

    private void batchUpdateEnable(List<AgentBuilderOperateQuickCommandReq> req) {
        if (CollectionUtil.isEmpty(req)) {
            return;
        }
        log.info("batchUpdateEnable start, size: {}", req.size());
        List<QuickCommandEntity> commandEntities = new ArrayList<>();
        for (AgentBuilderOperateQuickCommandReq AgentBuilderOperateQuickCommandReq : req) {
            QuickCommandEntity commandEntity = getCommandEntity(AgentBuilderOperateQuickCommandReq.getCommandId(),
                AgentBuilderOperateQuickCommandReq);
            commandEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            commandEntity.setLastUpdatedByUserId(AuthorizationContextHolder.userId());
            commandEntities.add(commandEntity);
        }
        quickCommandMapper.batchUpdateEnable(commandEntities);
    }

    private void batchInsert(List<AgentBuilderOperateQuickCommandReq> req) {
        if (CollectionUtil.isEmpty(req)) {
            return;
        }
        log.info("batchInsert start, size: {}", req.size());
        List<QuickCommandEntity> commandEntities = new ArrayList<>();
        for (AgentBuilderOperateQuickCommandReq AgentBuilderOperateQuickCommandReq : req) {
            QuickCommandEntity commandEntity = getCommandEntity(AppCommonUtils.getUUID(),
                AgentBuilderOperateQuickCommandReq);
            commandEntity.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            commandEntity.setCreatedByUserId(AuthorizationContextHolder.userId());
            commandEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            commandEntity.setLastUpdatedByUserId(AuthorizationContextHolder.userId());
            commandEntities.add(commandEntity);
        }

        quickCommandMapper.batchInsert(commandEntities);
    }

    private void batchUpdate(List<AgentBuilderOperateQuickCommandReq> req) {
        if (CollectionUtil.isEmpty(req)) {
            return;
        }
        log.info("batchUpdate start, size: {}", req.size());
        List<QuickCommandEntity> commandEntities = new ArrayList<>();
        for (AgentBuilderOperateQuickCommandReq AgentBuilderOperateQuickCommandReq : req) {
            QuickCommandEntity commandEntity = getCommandEntity(AgentBuilderOperateQuickCommandReq.getCommandId(),
                AgentBuilderOperateQuickCommandReq);
            commandEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            commandEntity.setLastUpdatedByUserId(AuthorizationContextHolder.userId());
            commandEntities.add(commandEntity);
        }
        quickCommandMapper.batchUpdate(commandEntities);
    }

    private QuickCommandEntity getCommandEntity(String id, AgentBuilderOperateQuickCommandReq req) {
        QuickCommandEntity quickCommandEntity = new QuickCommandEntity();
        quickCommandEntity.setId(id);
        quickCommandEntity.setAgentId(req.getAgentId());
        quickCommandEntity.setName(req.getQuickCommandName());
        quickCommandEntity.setActive(req.getActive());
        quickCommandEntity.setDescription(req.getDescription());
        quickCommandEntity.setContent(req.getContent());
        quickCommandEntity.setDisplayNo(req.getDisplayNo());
        quickCommandEntity.setRecommend(req.getRecommend());
        quickCommandEntity.setDomainId(AuthorizationContextHolder.domainId());
        return quickCommandEntity;
    }
}
