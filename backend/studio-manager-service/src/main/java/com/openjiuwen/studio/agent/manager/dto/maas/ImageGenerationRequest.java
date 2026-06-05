/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.maas;

import lombok.Data;

/**
 * 图像生成请求体类
 *
 */
@Data
public class ImageGenerationRequest {
    private String model;

    private String prompt;

    private String size;
}
