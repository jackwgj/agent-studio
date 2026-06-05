/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.ImmutableList;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class MaasEmbeddingRequestAdaptorTest {
    private final MaasEmbeddingRequestAdaptor maasEmbeddingRequestAdaptor = new MaasEmbeddingRequestAdaptor();

    @Test
    void should_return_maas_embedding_then_get_name() {
        assertEquals("maas_embedding", maasEmbeddingRequestAdaptor.getName());
    }

    @Test
    void should_return_null_when_convert_request_using_null_obj() {
        assertNull(maasEmbeddingRequestAdaptor.requestBodyConvert(Collections.emptyMap(), null, false));
    }

    @Test
    public void should_do_nothing_when_convert_request_using_un_embedding_request() {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();

        assertEquals(chatCompletionRequest,
            maasEmbeddingRequestAdaptor.requestBodyConvert(Collections.emptyMap(), chatCompletionRequest, false));
    }

    private static final String TEST_MODEL = "embedding";

    private static final String TEST_USER = "user";

    private static final String TEST_SINGLE_INPUT = "input";

    private static final List<String> TEST_MULTI_INPUT = ImmutableList.of("input_1", "input_2");

    @Test
    public void should_return_converted_embedding_request_when_convert_using_embedding_request_with_single_input() {
        EmbeddingRequest embeddingRequest = new EmbeddingRequest();
        embeddingRequest.setModel(TEST_MODEL);
        embeddingRequest.setUser(TEST_USER);
        embeddingRequest.setInput(TEST_SINGLE_INPUT);

        Object convertedRequest = maasEmbeddingRequestAdaptor.requestBodyConvert(Collections.emptyMap(),
            embeddingRequest, false);

        assertNotNull(convertedRequest);
        assertInstanceOf(MaasEmbeddingRequestAdaptor.Request.class, convertedRequest);
        assertEquals(TEST_MODEL, ((MaasEmbeddingRequestAdaptor.Request) convertedRequest).getModel());
        assertEquals(Collections.singletonList(TEST_SINGLE_INPUT),
            ((MaasEmbeddingRequestAdaptor.Request) convertedRequest).getInput());
    }

    @Test
    public void should_return_converted_embedding_request_when_convert_using_embedding_request_with_multi_input() {
        EmbeddingRequest embeddingRequest = new EmbeddingRequest();
        embeddingRequest.setModel(TEST_MODEL);
        embeddingRequest.setUser(TEST_USER);
        embeddingRequest.setInput(TEST_MULTI_INPUT);

        Object convertedRequest = maasEmbeddingRequestAdaptor.requestBodyConvert(Collections.emptyMap(),
            embeddingRequest, false);

        assertNotNull(convertedRequest);
        assertInstanceOf(MaasEmbeddingRequestAdaptor.Request.class, convertedRequest);
        assertEquals(TEST_MODEL, ((MaasEmbeddingRequestAdaptor.Request) convertedRequest).getModel());
        assertEquals(TEST_MULTI_INPUT, ((MaasEmbeddingRequestAdaptor.Request) convertedRequest).getInput());
    }
}
