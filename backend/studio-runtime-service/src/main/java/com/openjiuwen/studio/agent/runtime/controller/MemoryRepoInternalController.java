/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MemoryRepoInternalController {
    @Resource
    private JiuWenClient jiuWenClient;

    @DeleteMapping("/internal/v1/memory-repos/{memory_repo_id}")
    public ResponseEntity<Object> deleteMemoryRepoData(@PathVariable("memory_repo_id") String memoryRepoId) {
        log.info("Forwarding memory repo deletion to jiuwen runtime: {}", memoryRepoId);
        return jiuWenClient.deleteMemoryRepoData(memoryRepoId);
    }
}
