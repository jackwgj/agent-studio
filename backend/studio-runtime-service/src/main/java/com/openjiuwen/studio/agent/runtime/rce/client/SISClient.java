/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.common.dto.run.AsrReq;
import com.openjiuwen.studio.agent.common.dto.run.AsrRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.GetLASRJobResultRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.SubmitLASRJobReq;
import com.openjiuwen.studio.agent.runtime.rce.model.SubmitLASRJobRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "sisClient", url = "${feign.client.config.sisClient.url:}")
public interface SISClient {
    @PostMapping("/v1/{project_id}/asr/transcriber/jobs")
    SubmitLASRJobRsp submitLASRJob(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @RequestBody SubmitLASRJobReq body);

    @GetMapping("/v1/{project_id}/asr/transcriber/jobs/{job_id}")
    GetLASRJobResultRsp getLASRJobResult(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "job_id") String jobId);

    @PostMapping("/v1/{project_id}/asr/short-audio")
    AsrRsp shortAudioRecognition(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @RequestBody AsrReq body);
}
