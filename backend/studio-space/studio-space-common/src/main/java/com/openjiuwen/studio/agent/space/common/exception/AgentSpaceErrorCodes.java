/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 开发中心错误码
 * 0000~1000：系统型错误码，错误正常上报，计入稳定性监控及告警
 * 1001~9999：业务性错误码，不计入接口稳定性及告警
 */
public enum AgentSpaceErrorCodes {
    DEPLOY_AGENT_FAILED("0000", "部署Agent失败", HttpStatus.BAD_REQUEST),
    K8S_IO_EXCEPTION("0001", "{}", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_OBS_EXECUTE_ERROR("0002", "OBS接口调用异常，详情如下：{}", HttpStatus.BAD_REQUEST),
    K8S_API_EXCEPTION("0003", "{}", HttpStatus.BAD_REQUEST),
    HTTP_REQUEST_ERROR("0004", "{}", HttpStatus.BAD_REQUEST),
    CONNECTION_FAILED("0005", "HTTP连接失败", HttpStatus.BAD_REQUEST),
    CONNECTION_TIME_OUT("0006", "HTTP连接超时", HttpStatus.BAD_REQUEST),
    SSL_CHECK_FAILED("0007", "SSL校验失败", HttpStatus.BAD_REQUEST),
    LOAD_KEY_STORE_FAILED("0008", "加载密钥库失败!", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_ERROR("0010", "系统繁忙，请稍后再试！", HttpStatus.INTERNAL_SERVER_ERROR),
    HIS_CONFIG_LOAD_FAILED("0011", "HIS配置中心连接失败，请联系管理员！", HttpStatus.INTERNAL_SERVER_ERROR),
    AGENT_BUILDER_ACCESS_INTERNAL_ERROR("0011", "访问内部服务报错，详情如下：{}", HttpStatus.BAD_REQUEST),
    COMMON_ERROR("1001", "{}", HttpStatus.INTERNAL_SERVER_ERROR),
    CONFIG_VALUE_IS_EMPTY("1003", "配置参数{}为空，请联系管理员", HttpStatus.UNAUTHORIZED),
    URI_INVALID("1009", "URI非法", HttpStatus.BAD_REQUEST),
    URL_QUERY_PARAM_DUPLICATE("1010", "URI请求参数重复！", HttpStatus.BAD_REQUEST),
    URL_QUERY_PARAM_ERROR("1011", "URI请求参数错误！", HttpStatus.BAD_REQUEST),
    NOT_LOGIN("1016", "用户未登录或登录会话过期，请重新登录！", HttpStatus.BAD_REQUEST),
    USER_LOGOUT("1017", "用户登出！", HttpStatus.BAD_REQUEST),
    IAM_TICKET_EMPTY("1018", "Iam Ticket Empty!", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_PARAM_INCORRECT_ERROR("1020", "参数校验错误，详情如下：{}。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TENANT_ID_EMPTY_ERROR("1021", "domain-id 为空。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_FILE_NOT_SUPPORT_ERROR("1024", "文件类型不支持。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_FILE_DOWNLOAD_FAIL_ERROR("1026", "文件下载失败。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_USER_ID_EMPTY_ERROR("1028", "user-id 为空。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_FILE_SIZE_EXCEED_LIMIT_ERROR("1029", "文件{}大小超过限制{}", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_ALL_FILE_SIZE_EXCEED_LIMIT_ERROR("1030", "所有文件总大小超过限制 {}", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_CHAT_REPEAT_ERROR("1036", "已在其他会话中进行对话。", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_SERVICE_INTERNAL_ERROR("1037", "服务内部错误，详情如下：{}", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_EXCEEDING_QUOTA_ERROR("1038", "租户{}中的资源{}超出配额，配额为{}", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_RUNNING_TASK_EXCEED_LIMIT_ERROR("1039", "运行任务并发数已达到上限", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_RUNNING_TASK_EXCEED_LIMIT_FOR_USER_ERROR("1040", "用户{}运行任务并发数达到上限{}，当前是{}",
        HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_NOT_RUNNING("1041", "当前任务不在运行中，无法暂停", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_CANNOT_STOP_ERROR("1042", "任务{}当前状态为{}，无法手动停止", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_PIN_LIMIT_ERROR("1043", "任务置顶数已达上限{}", HttpStatus.BAD_REQUEST),
    K8S_SERVICE_EXIST("1044", "K8S服务已存在", HttpStatus.BAD_REQUEST),
    K8S_DEPLOYMENT_EXIST("1045", "K8S服务部署已存在", HttpStatus.BAD_REQUEST),
    JSON_PROCESS_ERROR("1047", "json解析失败", HttpStatus.BAD_REQUEST),
    FILE_NAME_INVALID("1048", "文件名不合法!", HttpStatus.BAD_REQUEST),
    FILE_SIZE_ERROR("1049", "文件大小超过最大限制", HttpStatus.BAD_REQUEST),
    FILE_TYPE_ERROR("1050", "文件类型不合法！", HttpStatus.BAD_REQUEST),
    FILE_NOT_SELECT("1051", "请上传文件！", HttpStatus.BAD_REQUEST),
    FILE_INVALID("1052", "文件不合法！", HttpStatus.BAD_REQUEST),
    FILE_ID_INVALID("1053", "文件ID不合法！", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_STATUS_INCORRECT("1054", "任务不能重复执行。", HttpStatus.BAD_REQUEST),
    IAM_TOKEN_CERT_DOWNLOAD_FAILED("1055", "IAM证书下载失败，请联系管理员", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_INITIALIZING("1056", "任务正在初始化，无法暂停，请稍后重试", HttpStatus.BAD_REQUEST),
    AGENT_BUILDER_TASK_CANNOT_PAUSE_ERROR("1057", "无法暂停任务", HttpStatus.BAD_REQUEST),
    REQUEST_PARAM_IS_NULL("1058", "请求参数：{} 为空", HttpStatus.BAD_REQUEST),
    OPENAPI_AUTH_FAILED("1059", "认证失败!", HttpStatus.BAD_REQUEST),
    DO_EXTERNAL_API_ERROR("1060", "调用外部接口失败，请联系管理员", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("1061", "文件上传失败请联系管理员，请联系管理员！", HttpStatus.BAD_REQUEST),
    POST_SSE_EXCEPTION("1062", "post sse api error, {}", HttpStatus.INTERNAL_SERVER_ERROR),
    K8S_SERVICE_NOT_EXIST("1063", "K8S服务不存在", HttpStatus.BAD_REQUEST),
    NOT_ENABLED_AGENT_BUILDER_SERVICE("1064", "当前租户未开通AgentBuilder服务，请开通后使用", HttpStatus.INTERNAL_SERVER_ERROR),
    AGENT_DATA_NOT_EXIST("1065", "数据不存在", HttpStatus.BAD_REQUEST),
    AGENT_DATA_STATUS_INOPERABLE("1066", "agent状态无法操作", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_NAME_NOT_EXIST("1067", "快捷指令名称不存在", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_NAME_EXIST("1068", "快捷指令名称已存在", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_CONTENT_NOT_EXIST("1069", "快捷指令内容不存在", HttpStatus.BAD_REQUEST),
    AGENT_ID_NOT_EXIST("1070", "应用id不存在", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_NAME_DUPLICATE("1071", "快捷指令名称重复: {}", HttpStatus.BAD_REQUEST),
    AGENT_NOT_EXIST("1072", "应用不存在: {}", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_ID_NOT_EXIST("1073", "快捷指令id不存在", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_NOT_EXIST("1074", "快捷指令不存在", HttpStatus.BAD_REQUEST),
    QUICK_COMMAND_OPERATION_DUPLICATE("1075", "重复操作快捷指令: {}", HttpStatus.BAD_REQUEST),
    NO_OPERATION_PERMISSION("1076", "无操作权限", HttpStatus.FORBIDDEN),
    FREEZE_TENANT("1077", "账号已被冻结", HttpStatus.FORBIDDEN),
    GET_IAM_CONTEXT_FAILED("1080", "获取 IAM 上下文失败", HttpStatus.BAD_REQUEST),
    ;

    private final String errorCode;

    private final String errorMsg;

    private final HttpStatus httpStatus;

    AgentSpaceErrorCodes(String errorCode, String errorMsg, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }

    public String errorCode() {
        return "AgentBuilder.0600" + errorCode;
    }

    public String errorMsg() {
        return errorMsg;
    }

    public HttpStatus httpStatus() {
        return this.httpStatus;
    }
}
