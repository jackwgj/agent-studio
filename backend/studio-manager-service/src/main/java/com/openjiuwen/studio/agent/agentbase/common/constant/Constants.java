/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.common.constant;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * 常量
 *
 */
public interface Constants {
    /**
     * JsonString 解析 Map
     */
    @SuppressWarnings( {"checkstyle: all"})
    TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() { };

    /**
     * lakeSearch endpoint
     */
    String LAKE_SEARCH_ENDPOINT
        = "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search";

    /**
     * 中文语言id
     */
    String ZH_LANGUAGE_ID = "zh";

    /**
     * 使用kerberos认证的lakeSearch实现类名
     */
    String LAKE_SEARCH_SERVICE_NAME_WITH_KERBEROS = "KerLakeSearch";

    /**
     * 图片ID在redis缓存中key的前缀
     */
    String KNOWLEDGE_IMAGE_CACHE_PREFIX = "knowledge:image:";

    /**
     * 文件id在redis缓存中key的前缀
     */
    String KNOWLEDGE_FILE_CACHE_PREFIX = "knowledge:file:";

    String KNOWLEDGE_FILE_TYPE_DOC = "doc";

    String KNOWLEDGE_FILE_TYPE_FAQ = "faq";

    String SINGLE_FILE_SIZE = "FILE_SIZE";

    String OBS_CAPACITY = "OBS_CAPACITY";

}
