package com.openjiuwen.studio.agent.space.app.task;

import com.openjiuwen.studio.agent.space.app.constant.Constant;
import com.openjiuwen.studio.agent.space.app.constant.RedisKeyConstant;
import com.openjiuwen.studio.agent.space.app.enums.DRTaskStatusType;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderTaskMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class DRTaskHandler {
    @Resource
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Resource
    private RedisClient redisClient;

    /**
     * 更新数据库和Redis中的任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     */
    public boolean updateTaskStatus(@NotNull String taskId, @NotNull Integer status) {
        return updateTaskStatusCore(taskId, status, false);
    }

    /**
     * 更新数据库和Redis中的任务状态（带删除标记）
     *
     * @param taskId 任务ID
     * @param status 状态（可为null）
     */
    public boolean updateTaskStatusWithDelete(@NotNull String taskId, @NotNull Integer status) {
        return updateTaskStatusCore(taskId, status, true);
    }

    /**
     * 根据agent返回的数据更新taskName
     * 当任务名称不为默认值时不能更新
     *
     * @param taskId   任务ID
     * @param taskName agent返回的taskName
     */
    public int updateTaskNameByAgent(@NotNull String taskId, @NotNull String taskName) {
        AgentBuilderTaskEntity entity = new AgentBuilderTaskEntity();
        entity.setId(taskId);
        entity.setName(taskName.substring(0, Math.min(taskName.length(), 64)));
        return AgentBuilderTaskMapper.updateTaskNameByAgent(entity);
    }

    /**
     * 更新任务状态的核心逻辑
     *
     * @param taskId 任务ID
     * @param status 状态值
     * @param isDeleted 是否删除
     */
    private boolean updateTaskStatusCore(String taskId, Integer status, boolean isDeleted) {
        log.info("[updateTaskStatusCore] start. taskId: {}, status: {}, isDeleted: {}", taskId, status, isDeleted);

        AgentBuilderTaskEntity AgentBuilderTaskEntity = buildTaskEntity(taskId, status);

        int count = isDeleted
            ? AgentBuilderTaskMapper.deletedById(AgentBuilderTaskEntity)
            : AgentBuilderTaskMapper.updateStatusById(AgentBuilderTaskEntity);
        if (count != 1) {
            log.error("[updateTaskStatusCore] AgentBuilderTaskEntity failed to store the database. taskId: {}, status: {}",
                taskId, status);
            return false;
        }

        if (status != null) {
            redisClient.set(String.format(RedisKeyConstant.DR_STATUS, taskId), String.valueOf(status),
                Duration.ofSeconds(Constant.REDIS_EXPIRE_TIME));
        } else {
            if (isDeleted) {
                redisClient.set(String.format(RedisKeyConstant.DR_STATUS, taskId),
                    String.valueOf(DRTaskStatusType.DELETED.getStatus()),
                    Duration.ofSeconds(Constant.REDIS_EXPIRE_TIME));
            }
        }
        return true;
    }

    /**
     * 构建任务实体
     */
    private AgentBuilderTaskEntity buildTaskEntity(String taskId, Integer status) {
        AgentBuilderTaskEntity entity = new AgentBuilderTaskEntity();
        entity.setId(taskId);
        if (status != null) {
            entity.setStatus(status);
        }
        AppCommonUtils.setCommonUpdateProperties(entity);
        return entity;
    }
}
