/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.rce.models.PythonInterpreterReq;
import com.openjiuwen.studio.agent.manager.rce.models.PythonInterpreterRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * CIS（code interpreter service）服务的client
 *
 */
@FeignClient(name = "pythonInterpreter", url = "${feign.client.config.pythonInterpreter.url:}")
public interface PythonInterpreterClient {

    /**
     * 沙箱服务调用接口
     *
     * @param pythonInterpreterReq 沙箱服务请求体
     * @return PythonInterpreterRsp 沙箱服务响应
     */
    @PostMapping("/query")
    PythonInterpreterRsp getPythonInterpreterResult(
        @RequestHeader("X-Auth-Token") String authToken,
        @RequestHeader("X-Language") String language,
        @RequestBody PythonInterpreterReq pythonInterpreterReq);
}