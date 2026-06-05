/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.handler;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobNeedBePausedException;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobPausedException;

import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述
 *
 */
@Slf4j
public abstract class AbstractCanPartlySuccessJobHandler extends AbstractJobHandler {

    @Override
    protected void doExecute(JobInstance jobInstance, String token) throws Exception {
        try {
            doExecuteInternal(jobInstance, token);
        } catch (JobNeedBePausedException e) {
            onNeedPaused(jobInstance);
        }
    }

    protected abstract void doExecuteInternal(JobInstance jobInstance, String token) throws Exception;

    @Override
    protected void onSuccess(JobInstance jobInstance) {
        if (ifAllSuccess(jobInstance)) {
            processSuccess(jobInstance);
        } else if (ifNeedBePausedById(jobInstance)) {
            onNeedPaused(jobInstance);
            log.info("===Won't be there!!!==The job is paused, job id: {}, task id: {}", jobInstance.getId(),
                jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID));
        } else if (ifPausedById(jobInstance)) {
            log.info("The job is paused, job id: {}, task id: {}", jobInstance.getId(),
                jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID));
        } else if (ifAllFailed(jobInstance)) {
            processError(jobInstance, "All " + failedErrorMsg(jobInstance));
        } else {
            // 部分成功
            if (ifPartlySuccessRetryEnabled(jobInstance) && canRetry(jobInstance)) {
                log.info("The job is partlySuccess, can retry, job id: {}", jobInstance.getId());
                onRetry(jobInstance);
            } else {
                log.info("The job is partlySuccess, can not retry again, job id: {}", jobInstance.getId());
                onPartlySuccess(jobInstance, "Partly success " + failedErrorMsg(jobInstance));
            }
        }
    }

    protected abstract void processSuccess(JobInstance jobInstance);

    protected abstract void onRetry(JobInstance jobInstance);

    protected abstract void onPartlySuccess(JobInstance jobInstance, String errorMsg);

    protected abstract void onNeedPaused(JobInstance jobInstance);

    protected void checkPaused(JobInstance jobInstance) {
        if (ifNeedBePausedById(jobInstance)) {
            throw new JobNeedBePausedException();
        }

        if (ifPausedById(jobInstance) || ifAllSuccess(jobInstance)) {
            throw new JobPausedException();
        }
    }

    protected abstract boolean ifNeedBePausedById(JobInstance jobInstance);

    protected abstract boolean ifPausedById(JobInstance jobInstance);

    protected abstract boolean ifAllSuccess(JobInstance jobInstance);

    protected abstract void processError(JobInstance jobInstance, String errorMsg);

    protected String failedErrorMsg(JobInstance jobInstance) {
        return "All Failed!" + JSONObject.toJSONString(jobInstance.getContext());
    }

    protected abstract boolean ifPartlySuccessRetryEnabled(JobInstance jobInstance);

    protected abstract boolean ifAllFailed(JobInstance jobInstance);

}
