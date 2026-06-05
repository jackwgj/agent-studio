/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.model;

/**
 * 响应状态
 *
 * 
 * @since 2023-07-18
 */
public enum ResponseStatus {
    OK(200, "success"),
    FAIL_NORMAL(500, "internal error"),
    REQUEST_ERROR(400, "bad request"),
    REQUEST_NO_AUTHINFO(401, "auth failed"),
    REQUEST_VISIT_FORBIDDEN(403, "forbidden visit"),
    REQUEST_NOT_EXIST(404, "resource not found"),
    NO_AUTHORIZATION_NOT(406, "authorization error"),
    REQUEST_CONFLICT(409, "request conflict"),
    REQUEST_RESTRICTION(411, "request restricted"),

    /* *数据库* */
    DATA_CREATE_ERROR(100, "add data failed"),
    DATA_REQUERY_ERROR(101, "query data failed"),
    DATA_UPDATED_ERROR(102, "update data failed"),
    DATA_DELETED_ERROR(103, "delete data failed"),
    DATA_EXIST(104, "data exist"),
    DATA_NOT_EXIST(105, "data not exist"),
    TEAM_SPACE_NOT_EXIST(412, "team space.not exist");

    private final int code;

    private final String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseStatus getResponseStatus(int code) {
        ResponseStatus[] values = ResponseStatus.values();
        for (ResponseStatus responseStatus : values) {
            if (responseStatus.getCode() == code) {
                return responseStatus;
            }
        }
        return null;
    }
}
