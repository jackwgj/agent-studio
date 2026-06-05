/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

/**
 * Response status code enumeration
 */
public enum ResponseCode {
    /**
     * continue
     */
    CONTINUE(100, "Continue"),

    /**
     * success
     */
    OK(200, "OK"),

    /**
     * Created
     */
    CREATED(201, "Created"),

    /**
     * Accepted
     */
    ACCEPTED(202, "Accepted"),

    /**
     * No Content
     */
    NO_CONTENT(204, "NO Content"),

    /**
     * Bad Request
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * Unauthorized
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * Forbidden
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * Not Found
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * Method Not Allowed
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    /**
     * Not Acceptable
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),

    /**
     * Conflict
     */
    CONFLICT(409, "Conflict"),

    /**
     * Gone
     */
    GONE(410, "Gone"),

    /**
     * Precondition Failed
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),

    /**
     * Too many requests
     */
    TOO_MANY_REQUESTS(429, "Too many requests"),

    /**
     * Internal Server Error
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    /**
     * Not Impemented
     */
    NOT_IMPEMENTED(501, "Not Impemented"),

    /**
     * Service Unavailable
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private int code;

    private String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ResponseCode{" + "code=" + code + ", msg='" + msg + '\'' + '}';
    }
}
