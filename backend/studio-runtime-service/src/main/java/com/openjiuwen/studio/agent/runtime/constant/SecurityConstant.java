/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.constant;

/**
 * 安全常量
 *
 */
public class SecurityConstant {

    /**
     * 文本审核
     */
    public final static String IDENTITY_TYPE_TEXT = "0";

    /**
     * 图像审核
     */
    public final static String IDENTITY_TYPE_IMAGE = "1";

    /**
     * 音频审核
     */
    public final static String IDENTITY_TYPE_AUDIO = "2";

    /**
     * 视频审核
     */
    public final static String IDENTITY_TYPE_VIDEO = "3";

    /**
     * 内容审核缓存Key
     */
    public final static String CONTENT_IDENTITY_CACHE_KEY = "WS_CONTENT_IDENTITY_";

    /**
     * 内容审核缓存失效时间
     */
    public final static String SYS_PARAMS_CONTENT_IDENTITY_CACHE_EXPIRATION_TIME = "CONTENT_IDENTITY_CACHE_EXPTIME";

    /**
     * 系统参数：SYS_SECURITY_RETRY_TIMES
     */
    public static final String SYS_SECURITY_RETRY_TIMES = "SYS_SECURITY_RETRY_TIMES";
}
