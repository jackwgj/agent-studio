/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库连接的参数值
 *
 * @since 2025-08-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeBaseConnectionParam {

    private String code;

    private String value;

}
