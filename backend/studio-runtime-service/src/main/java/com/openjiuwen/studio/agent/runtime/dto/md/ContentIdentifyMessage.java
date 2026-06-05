/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentIdentifyMessage {
    private String text;

    private String imageUrl;

    private String imageName;

    private String digestValue;

    private String audioUrl;

    private String languageCode;
}
