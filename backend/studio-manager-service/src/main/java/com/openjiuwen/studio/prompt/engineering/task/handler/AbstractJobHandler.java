/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.handler;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.prompt.engineering.mapper.JobInstanceMapper;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.log.LogContextLocalMgr;
import com.openjiuwen.studio.prompt.engineering.task.repository.JobInstanceRepository;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * 功能描述
 *
 */
@Slf4j
public abstract class AbstractJobHandler implements IJobHandler {
    @Resource
    protected JobInstanceMapper jobInstanceMapper;

    @Autowired
    protected JobInstanceRepository jobInstanceRepository;

    @Autowired
    private I18nUtil i18nUtil;

    @Override
    public void execute(JobInstance jobInstance, String token) {
        try {
            LogContextLocalMgr.setTraceID(jobInstance.getId());
            // 加在这里比较Low，考虑升级下小门神
            MDC.put("traceId", jobInstance.getId());
            onStart(jobInstance);
            doExecute(jobInstance, token);
            onSuccess(jobInstance);
        } catch (AgentStudioException e) {
            log.error("Meet error when execute " + jobInstance.getContext().getValue(JobParamKeys.JOB_SPEC_NAME), e);
            onError(jobInstance, i18nUtil.getMessage(e.getErrorCode(), e.getArgs()));
        }  catch (Exception e) {
            log.error("Meet error when execute " + jobInstance.getContext().getValue(JobParamKeys.JOB_SPEC_NAME), e);
            onError(jobInstance, e);
        } finally {
            LogContextLocalMgr.clear();
            MDC.clear();
        }
    }

    protected void onError(JobInstance jobInstance, Exception e) {
        onError(jobInstance, StringUtils.defaultIfBlank(e.getMessage(), "Null pointer exception."));
    }

    protected void onError(JobInstance jobInstance, String errorMsg){
        // 1. 记录失败信息
        // 2. 即将要做，通知JobManager我这个任务失败了，要看下是否允许重试。当前实现是handler自己判断是否可以重试。
        // 3. 如果可以重试，则设置状态为retrying
        // 4. 如果不可以重试，则设置状态为failed;
        jobInstance.getContext().put(JobParamKeys.ERROR_MSG, errorMsg);

        // 重试逻辑屏蔽
        log.info("The job is failed, job id: {}", jobInstance.getId());
        jobInstanceRepository.failed(jobInstance);
        onErrorExtra(jobInstance, errorMsg);
    }

    protected boolean canRetry(JobInstance jobInstance) {
        int nowRetryTime = jobInstance.getRetryTime();
        int maxRetryTime = jobInstance.getContext().getValueAsInteger(JobParamKeys.MAX_RETRY_TIME);

        return nowRetryTime + 1 <= maxRetryTime;
    }

    protected abstract void onSuccess(JobInstance jobInstance);

    protected abstract void onErrorExtra(JobInstance jobInstance, String errorMsg);

    protected void onStart(JobInstance jobInstance){
        jobInstanceRepository.start(jobInstance);
    }

    protected abstract void doExecute(JobInstance jobInstance, String token) throws Exception;


    protected void updateContext(JobInstance jobInstance, String key, String value) {
        // 更新context信息
        Context context = jobInstance.getContext();
        context.getStrategy(key).setUpdatable(true);
        context.update(key, value);
        jobInstance.setContext(context);
        jobInstanceRepository.updateContext(jobInstance);
    }
}
