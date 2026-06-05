/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license.models;

import com.openjiuwen.studio.agent.common.enums.StudioError;

import lombok.Getter;

/**
 * License状态
 *
 */
public enum LicenseState {
    /**
     * License 试用期
     */
    LS_TRIAL(-1, "trial"),
    /**
     * License 有效
     */
    LS_VALID(0, "valid"),
    /**
     * 宽限期
     */
    LS_GRACE(1, "valid and in grace time"),
    /**
     * License 保存失败
     */
    LS_UNSAVED(2, "save to obs failed"),
    /**
     * License 缺失
     */
    LS_MISSING(3, "license is missing"),
    /**
     * License 无效或超期
     */
    LS_INVALID(4, "license is invalid"),
    /**
     * 资源使用超过限制
     */
    LS_EXCEED(5, "license resource exceed"),
    /**
     * 获取 pod 资源错误
     */
    LS_PODS_FAILED(6, "get pods info failed");

    @Getter
    private final int code;

    @Getter
    private final String message;

    LicenseState(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "LicenseState{code: " + code + ", message: " + message + "}";
    }

    public StudioError getStudioError() {
        switch (this) {
            case LS_UNSAVED -> {
                return StudioError.LICENSE_UNSAVED;
            }
            case LS_MISSING -> {
                return StudioError.LICENSE_MISSING;
            }
            case LS_EXCEED -> {
                return StudioError.LICENSE_EXCEED;
            }
            case LS_PODS_FAILED -> {
                return StudioError.LICENSE_PODS_ERROR;
            }
            default -> {
                return StudioError.LICENSE_INVALID;
            }
        }
    }

    public static LicenseState fromCode(int code) {
        for (LicenseState status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown code of LicenseState: " + code);
    }
}
