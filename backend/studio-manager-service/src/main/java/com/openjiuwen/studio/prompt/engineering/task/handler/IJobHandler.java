/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.handler;


import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;

/**
 * 任务接口
 *
 */
public interface IJobHandler {

    void execute(JobInstance jobInstance, String token);

    String supportJobSpecName();
}
