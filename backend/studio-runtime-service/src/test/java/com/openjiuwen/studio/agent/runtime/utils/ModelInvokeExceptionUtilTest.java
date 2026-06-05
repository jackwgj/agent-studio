/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelInvokeExceptionUtilTest {
    @Test
    public void modelArtsExceptionTest() {
        AgentStudioException exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(400,
            "ModelArts.81001  Invalid request body.");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401,
            "ModelArts.81003 Invalid authorization header..");
        Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_DATA_INVALID, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401,
            "ModelArts.81004  Invalid request because you do not have access to it.");
        Assertions.assertEquals(StudioError.MD_NO_MODEL_ACCESS_PERMISSION, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401,
            "ModelArts.81005  The free quota has been used up.");
        Assertions.assertEquals(StudioError.MD_THIRD_FREE_QUOTA_USED_UP, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(403, "ModelArts.81011  May contain senstive ..");
        Assertions.assertEquals(StudioError.MD_MSG_CONTAIN_SENSITIVE, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(404, "ModelArts.81009  Invalid model.");
        Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_EXIST, exp.getErrorCode());
    }

    @Test
    public void commonHttpExceptionHandlerTest() {
        AgentStudioException exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(409,
            "81001  Invalid request body.");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401, "81003 Invalid authorization header..");
        Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_DATA_INVALID, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401,
            "81004  Invalid request because you do not have access to it.");
        Assertions.assertEquals(StudioError.MD_NO_MODEL_ACCESS_PERMISSION, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(401, "81005  The free quota has been used up.");
        Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_DATA_INVALID, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(403, "81011  May contain senstive ..");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(404, "81009  Invalid model.");
        Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_EXIST, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(429, "81009  余额不足 model.");
        Assertions.assertEquals(StudioError.MD_OUTSTANDING_FEEDS, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(429, "81009  model.");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(402, "81009  insufficient balance model.");
        Assertions.assertEquals(StudioError.MD_OUTSTANDING_FEEDS, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(402, "81009  insufficient.");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(404, "81009  insufficient balance model.");
        Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(500, "81009  insufficient balance model.");
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(400,
            "81009  requested inference service does not exist.");
        Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_EXIST, exp.getErrorCode());

        exp = ModelInvokeExceptionUtil.commonHttpExceptionHandler(400, "81001  Invalid request body.");
        Assertions.assertEquals(StudioError.INVOKE_MODEL_BAD_REQUEST, exp.getErrorCode());
    }
}
