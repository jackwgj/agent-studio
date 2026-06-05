/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.task;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.HistoryAgentEntity;
import com.openjiuwen.studio.agent.manager.mapper.HistoryWorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.repository.HistoryAgentRepository;
import com.openjiuwen.studio.agent.manager.repository.HistoryMappingRepository;
import com.openjiuwen.studio.agent.manager.repository.HistoryReleaseVersionRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ManagerResourceCleanTask {
    private static final String PRR_SOFT_DELETE_LOCK = "PRR_SOFT_DELETE_LOCK";

    /**
     * 分布式锁等待时间
     */
    private static final int LOCK_TIME = 10;

    @Value("${prr.soft-delete-ttl-days: 730}")
    private Integer softDeleteTtlDays;

    @Autowired
    private HistoryAgentRepository historyAgentRepository;

    @Autowired
    private HistoryMappingRepository historyMappingRepository;

    @Autowired
    private HistoryWorkflowMapper historyWorkflowMapper;

    @Autowired
    private MgObsService mgObsService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private HistoryReleaseVersionRepository historyReleaseVersionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 定时清理软删除的模板数据
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldSoftDeleteResource() {
        log.info("Start cleaning old soft deleted resources");
        RedisLock lock = null;
        try {
            lock = redisClient.getLock(PRR_SOFT_DELETE_LOCK);
            if (lock.tryLock(Duration.ofSeconds(LOCK_TIME))) {
                deleteManagerResource();
            } else {
                log.warn("failed to get {} lock", PRR_SOFT_DELETE_LOCK);
            }
        } catch (Exception e) {
            log.error("async delete manager resource failed!", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public void deleteManagerResource() {
        // 计算软删除数据的存留时间
        Date targetDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(softDeleteTtlDays));

        // 查询所有两年前软删除的AGENT ID
        List<HistoryAgentEntity> agentEntities = historyAgentRepository.findByUpdatedOnLessThan(targetDate);
        List<String> workflowIds = historyWorkflowMapper.findByUpdatedOnLessThan(targetDate);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 删除agent
            if (!CollectionUtils.isEmpty(agentEntities)) {
                for (HistoryAgentEntity agentEntity : agentEntities) {
                    // 删除obs文件
                    mgObsService.deleteObsObjects(CommonConstant.AGENT_TYPE + CommonConstant.FOLDER_SEPARATOR
                        + CommonConstant.Workflow.IR + CommonConstant.FOLDER_SEPARATOR + agentEntity.getAgentId());
                    mgObsService.deleteObsObjects(CommonConstant.AGENT_TYPE + CommonConstant.FOLDER_SEPARATOR
                        + CommonConstant.DSL_STR + CommonConstant.FOLDER_SEPARATOR + agentEntity.getAgentId());
                    historyMappingRepository.deleteByAppId(agentEntity.getAgentId());
                    historyReleaseVersionRepository.deleteByAppId(agentEntity.getAgentId());
                }
                historyAgentRepository.deleteAll(agentEntities);
                log.info("Successfully deleted {} agentIds permanently.", agentEntities.size());
            }

            // 删除workflow
            if (!CollectionUtils.isEmpty(workflowIds)) {
                for (String workflowId : workflowIds) {
                    mgObsService.deleteObsObjects(CommonConstant.WORKFLOW + CommonConstant.FOLDER_SEPARATOR
                        + CommonConstant.Workflow.IR + CommonConstant.FOLDER_SEPARATOR + workflowId);
                    historyMappingRepository.deleteByAppId(workflowId);
                    historyReleaseVersionRepository.deleteByAppId(workflowId);
                }
                historyWorkflowMapper.deleteByPrimaryKeys(workflowIds);
                log.info("Successfully deleted {} workflowIds permanently.", workflowIds.size());
            }
            transactionManager.commit(status);
        } catch (Exception e) {
            log.error("Failed to delete agent manager resource permanently.", e);
            transactionManager.rollback(status);
        }
    }
}
