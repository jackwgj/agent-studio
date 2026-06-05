/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import org.springframework.stereotype.Component;

/**
 * 多模态模型调用
 */
@Component("multiOpenApiRequestAdapterRun")
public class MultiOpenApiRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "multi_openai";
    }
}
