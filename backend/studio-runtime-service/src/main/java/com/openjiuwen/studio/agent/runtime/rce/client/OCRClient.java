/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.runtime.rce.model.SmartDocumentRecognizerReq;
import com.openjiuwen.studio.agent.runtime.rce.model.SmartDocumentRecognizerRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ocrClient", url = "${feign.client.config.ocrClient.url:}")
public interface OCRClient {
    @PostMapping("/v2/{project_id}/ocr/smart-document-recognizer")
    SmartDocumentRecognizerRsp smartDocumentRecognizer(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @RequestBody SmartDocumentRecognizerReq body);
}
