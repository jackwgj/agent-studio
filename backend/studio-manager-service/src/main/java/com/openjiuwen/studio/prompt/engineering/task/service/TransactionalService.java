/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动任务线程
 *
 */
@Service
public class TransactionalService {

    @Transactional
    public void execute(Runnable t) {
        t.run();
    }
}
