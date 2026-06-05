/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openjiuwen.studio.agent.space.common.constant.QuickCommandConstants;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;
import com.openjiuwen.studio.agent.space.dao.entity.QuickCommandEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 快捷指令操作类
 */
@Mapper
public interface QuickCommandMapper extends BaseMapper<QuickCommandEntity> {
    /**
     * 根据agentId查询智能体下的快捷指令列表
     *
     * @param agentId 智能体id
     * @param active  是否启用
     * @param key
     * @return 快捷指令列表
     */
    default List<QuickCommandEntity> getCommandListByAgentId(String agentId, String active, String key) {
        LambdaQueryWrapper<QuickCommandEntity> wrapper = new LambdaQueryWrapper<QuickCommandEntity>().eq(
                QuickCommandEntity::getAgentId, agentId)
            .eq(QuickCommandEntity::getDomainId, AuthorizationContextHolder.domainId())
            .eq(StringUtil.isNotEmptyString(active) && String.valueOf(QuickCommandConstants.ACTIVE_TRUE).equals(active),
                QuickCommandEntity::getActive, QuickCommandConstants.ACTIVE_TRUE)
            .like(StringUtil.isNotEmptyString(key), QuickCommandEntity::getName, key)
            .orderByAsc(QuickCommandEntity::getDisplayNo);
        return selectList(wrapper);
    }

    /**
     * 根据快捷指令名称和agentId查询快捷指令
     */
    default QuickCommandEntity getCommandByAgentIdAndName(String name, String agentId) {
        LambdaQueryWrapper<QuickCommandEntity> wrapper = new LambdaQueryWrapper<QuickCommandEntity>().eq(
                QuickCommandEntity::getName, name)
            .eq(QuickCommandEntity::getDomainId, AuthorizationContextHolder.domainId())
            .eq(QuickCommandEntity::getAgentId, agentId);
        return selectOne(wrapper);
    }

    /**
     * 批量插入快捷指令
     */
    default void batchInsert(List<QuickCommandEntity> commandEntities) {
        for (QuickCommandEntity commandEntity : commandEntities) {
            insert(commandEntity);
        }
    }

    /**
     * 批量修改快捷指令数据，如果为空则不修改
     */
    default void batchUpdate(@Param("objs") List<QuickCommandEntity> commandEntities) {
        for (QuickCommandEntity commandEntity : commandEntities) {
            LambdaUpdateWrapper<QuickCommandEntity> updateWrapper = new LambdaUpdateWrapper<QuickCommandEntity>().eq(
                    QuickCommandEntity::getId, commandEntity.getId())
                .eq(QuickCommandEntity::getDomainId, commandEntity.getDomainId())
                .set(StringUtil.isNotEmptyString(commandEntity.getName()), QuickCommandEntity::getName,
                    commandEntity.getName())
                .set(commandEntity.getActive() != null, QuickCommandEntity::getActive, commandEntity.getActive())
                .set(commandEntity.getDescription() != null, QuickCommandEntity::getDescription,
                    commandEntity.getDescription())
                .set(StringUtil.isNotEmptyString(commandEntity.getContent()), QuickCommandEntity::getContent,
                    commandEntity.getContent())
                .set(commandEntity.getDisplayNo() != null, QuickCommandEntity::getDisplayNo,
                    commandEntity.getDisplayNo())
                .set(commandEntity.getRecommend() != null, QuickCommandEntity::getRecommend,
                    commandEntity.getRecommend())
                .set(QuickCommandEntity::getLastUpdatedDate, commandEntity.getLastUpdatedDate())
                .set(QuickCommandEntity::getLastUpdatedByUserId, commandEntity.getLastUpdatedByUserId());
            update(updateWrapper);
        }
    }

    /**
     * 批量修改快捷指令开关
     */
    @Update("<script><foreach collection = 'objs' item = 'item' open='' separator=';'>"
        + "update t_quick_command set active = #{item.active}, last_updated_date = #{item.lastUpdatedDate},"
        + "last_updated_by_user_id = #{item.lastUpdatedByUserId}"
        + "where id = #{item.id} and domain_id = #{item.domainId}</foreach></script>")
    void batchUpdateEnable(@Param("objs") List<QuickCommandEntity> commandEntities);

    /**
     * 根据id查询快捷指令
     */
    default QuickCommandEntity getCommandById(String id) {
        LambdaQueryWrapper<QuickCommandEntity> wrapper = new LambdaQueryWrapper<QuickCommandEntity>().eq(
            QuickCommandEntity::getId, id).eq(QuickCommandEntity::getDomainId, AuthorizationContextHolder.domainId());
        return selectOne(wrapper);
    }

    /**
     * 根据agentId和displayNo倒叙查询快捷指令
     */
    default List<QuickCommandEntity> getCommandByAgentIdAndDisplayNoDesc(String agentId) {
        LambdaQueryWrapper<QuickCommandEntity> wrapper = new LambdaQueryWrapper<QuickCommandEntity>().eq(
            QuickCommandEntity::getAgentId, agentId).orderByDesc(QuickCommandEntity::getDisplayNo);
        Page<QuickCommandEntity> page = new Page<>(1, 1);
        return selectPage(page, wrapper).getRecords();
    }
}
