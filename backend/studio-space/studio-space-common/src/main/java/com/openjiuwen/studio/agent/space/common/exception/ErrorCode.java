/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public enum ErrorCode {

    /**
     * 服务内部错误，兜底的错误
     */
    SERVER_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0000", "Internal Server Error."),

    /**
     * obs连接失败
     */
    OBS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0003", "Obs service failed."),


    /**
     * 流式接口执行超时
     */
    STREAM_INTERFACE_EXECUTE_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0005", "Api request timeout"),

    /**
     * 构建SSLContext失败
     */
    SSL_CONTEXT_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0006", "Build ssl context failed."),

    /**
     * 获取IAM上下文失败
     */
    IAM_CONTEXT_OBTAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0007", "Get request iam context failed."),

    /**
     * 获取虚拟Token失败
     */
    VIRTUAL_TOKEN_OBTAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0008", "Get virtual token failed."),

    /**
     * 获取解析后Token失败
     */
    TOKEN_OBTAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0009", "Get token result error."),

    /**
     * 获取临时凭证失败
     */
    VIRTUAL_CREDENTIAL_OBTAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0010", "Get virtual credential error."),

    /**
     * 读取Token文件失败
     */
    IAM_TOKEN_READ_FILE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0011", "read token file error."),

    /**
     * 获取缓存Token失败
     */
    IAM_TOKEN_CACHE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0012", "Get cache key error."),

    /**
     * 获取Token失败
     */
    IAM_TOKEN_ANALYZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0013", "Analyze Iam token error."),

    /**
     * 生成Token失败
     */
    IAM_TOKEN_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0014", "Generate iam token error."),

    /**
     * 转换Token失败
     */
    IAM_TOKEN_CONVERT_USER_TO_DOMAIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0015",
        "Convert to domain error."),

    /**
     * 系统配置项错误
     */
    SYSTEM_PARAMS_CONFIG_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0016", "system_params_config_error."),

    /********************************************** 业务通用的一些错误码 *************************************************/

    /**
     * 方法参数校验异常
     */
    METHOD_ARGUMENT_INVALID(HttpStatus.BAD_REQUEST.value(), "1000", "Method parameter is invalid"),





    ;

    /********************************************** AgentSpace相关的错误码 end *************************************************/

    /**
     * 知识库的基础错误码
     */
    private static final String BASE_CODE = "0600";

    /**
     * 根据 code 查找枚举实例
     */
    private static final Map<String, ErrorCode> CODE_TO_ENUM = new HashMap<>();

    static {
        Set<String> uniqCodes = new HashSet<>(ErrorCode.values().length);
        Arrays.stream(ErrorCode.values()).forEach(e -> {
            if (uniqCodes.contains(e.code)) {
                throw new IllegalArgumentException("Duplicated error code for " + e);
            }
            uniqCodes.add(e.code);
        });
    }

    static {
        for (ErrorCode errorCode : values()) {
            CODE_TO_ENUM.put(errorCode.code, errorCode);
        }
    }

    /**
     * 错误码对应的http状态码
     */
    private final int httpCode;

    /**
     * 错误码对应的code信息，必须为4位数字
     * 其中0000~1000为系统型错误码，1001~9999为业务型错误码
     */
    private final String code;

    /**
     * 错误码对应的错误信息
     */
    private final String msg;

    /**
     * 构造器
     *
     * @param httpCode 对应的http状态码
     * @param code     长度为8的数字字符串，前三位是httpCode，后5位是我们服务内部细分的错误码
     * @param msg      错误描述信息
     */
    ErrorCode(int httpCode, String code, String msg) {
        this.httpCode = httpCode;
        this.code = "AgentBuilder." + BASE_CODE + code;
        this.msg = msg;
    }

    public static ErrorCode fromCode(String code) {
        ErrorCode error = CODE_TO_ENUM.get(code);
        if (error == null) {
            throw new IllegalArgumentException("未知的错误码: " + code);
        }
        return error;
    }
}
