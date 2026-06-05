/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.constant;

/**
 * 异常常量
 *
 */
public class ExceptionConstant {

    private static final String WISE_AGENT_ABBR = "AIAE";

    private static final String MODULE_ID_SECURITY = "0100";

    /**
     * 请求参数校验错误.
     */
    public static final String REQUEST_PARAMETER_VERIFICATION_ERROR = getErrorCode("1001");

    /**
     * 从DMQ获取结果超时
     */
    public static final String GET_DMQ_RESULT_TIME_OUT = getErrorCode("1002");

    /**
     * SCAS返回的异常
     */
    public static final String SCAS_ERROR = getErrorCode("1003");

    /**
     * 认证类型无效
     */
    public static final String IDENTITY_TYPE_IS_INVALID = getErrorCode("1004");

    /**
     * 1 ：参数校验错误，比如参数缺失，格式不对，长度超过限度等
     */
    public static final String SCAS_PARAMS_EXCEPTION = getErrorCode("1101");

    /**
     * 2 ：调用第三方失败，包括超时，除了 URL 错误之外的其他错误等
     */
    public static final String SCAS_THIRD_PARTY_ERROR = getErrorCode("1102");

    /**
     * 3 ：请求频次超过限制
     */
    public static final String SCAS_TOO_MANY_REQUESTS = getErrorCode("1103");

    /**
     * 4 ：图像下载失败
     */
    public static final String SCAS_DOWN_IMAGE_ERROR = getErrorCode("1104");

    /**
     * 5 ：SCAS 调用 ImageIdentify 直接返回失败，可能的情况是图片服务没启起来，超过流控限制等
     */
    public static final String SCAS_IMAGE_IDENTIFY_ERROR = getErrorCode("1105");

    /**
     * 10：图片格式异常，比如图片不支持、图片格式异常
     */
    public static final String SCAS_IMAGE_FORMAT_ERROR = getErrorCode("1110");

    /**
     * 14：图片大小异常（超出4M或为0M）
     */
    public static final String SCAS_SIZE_ERROR = getErrorCode("1114");

    /**
     * 音频下载失败
     */
    public static final String SCAS_DOWN_AUDIO_ERROR = getErrorCode("1115");

    /**
     * 音频大小异常（超出400M）
     */
    public static final String SCAS_AUDIO_SIZE_ERROR = getErrorCode("1116");

    public static String getErrorCode(String code) {
        return WISE_AGENT_ABBR + "." + MODULE_ID_SECURITY + code;
    }

    public static String getScasErrorCode(String code) {
        switch (code) {
            case "1":
                return SCAS_PARAMS_EXCEPTION;
            case "2":
                return SCAS_THIRD_PARTY_ERROR;
            case "3":
                return SCAS_TOO_MANY_REQUESTS;
            case "4":
                return SCAS_DOWN_IMAGE_ERROR;
            case "5":
                return SCAS_IMAGE_IDENTIFY_ERROR;
            case "10":
                return SCAS_IMAGE_FORMAT_ERROR;
            case "14":
                return SCAS_SIZE_ERROR;
            default:
                return getErrorCode(SCAS_ERROR);
        }
    }
}
