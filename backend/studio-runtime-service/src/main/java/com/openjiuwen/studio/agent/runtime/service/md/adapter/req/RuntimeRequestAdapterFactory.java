/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RuntimeRequestAdapterFactory
 *
 */
@Slf4j
@Service
public class RuntimeRequestAdapterFactory {
    private final Map<String, RequestAdapter> adapters;

    private final OpenApiRequestAdapter defaultAdapter;

    public RuntimeRequestAdapterFactory(List<RequestAdapter> adapterList, OpenApiRequestAdapter defaultAdapter) {
        adapters = new HashMap<>(adapterList.size());
        adapterList.forEach(adapter -> adapters.put(adapter.getName(), adapter));
        this.defaultAdapter = defaultAdapter;
    }

    /**
     * getAdapter
     *
     * @param protocol protocol
     * @return RequestAdapter
     */
    public RequestAdapter getAdapter(String protocol) {
        log.info("Try to get adaptor implement for protocol {}.", protocol);
        RequestAdapter adapter = adapters.get(protocol);
        if (adapter == null) {
            log.warn("Model Service INTERFACE_PROTOCOL is not support. protocol:{}", protocol);
            return defaultAdapter;
        }
        log.info("Got adaptor {} for protocol {}.", adapter, protocol);
        return adapter;
    }
}
