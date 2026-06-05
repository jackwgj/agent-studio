/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.runtime.enums.ModelRespContentType;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinimaxRequestAdapterTest {
    @Test
    public void getContentTypeTest() {
        JSONObject jsonObject = JSONObject.parseObject("{\"id\":\"050c61dc26892db2b086f74844bf29b5\",\""
            + "choices\":[{\"index\":0,\"delta\":{\"content\":\"你好\",\"role\":\"assistant\",\"name\":\"MM智能助理\""
            + ",\"audio_content\":\"\"}}],\"created\":1757228764,\"model\":\"abab6.5t-chat\",\"object\":\""
            + "chat.completion.chunk\",\"usage\":{\"total_tokens\":0,\"total_characters\":0},\"input_sensitive"
            + "\":false,\"output_sensitive\":false,\"input_sensitive_type\":0,\"output_sensitive_type\":0,\""
            + "output_sensitive_int\":0}");

        ModelRespContentType type = new MinimaxRequestAdapter().getContentType(jsonObject);
        Assertions.assertEquals(ModelRespContentType.CONTENT_SIGNAL, type);

        jsonObject = JSONObject.parseObject(
            "{\"id\":\"050c61dc26892db2b086f74844bf29b5\",\"choices\":[{\"finish_reason\":\"stop\",\"index\":0,\"delta\":{\"content\":\"呀，有什么我可以帮你的吗？（面带微笑，语气亲切）\",\"role\":\"assistant\",\"name\":\"MM智能助理\",\"audio_content\":\"\"}}],\"created\":1757228764,\"model\":\"abab6.5t-chat\",\"object\":\"chat.completion.chunk\",\"usage\":{\"total_tokens\":0,\"total_characters\":0},\"input_sensitive\":false,\"output_sensitive\":false,\"input_sensitive_type\":0,\"output_sensitive_type\":0,\"output_sensitive_int\":0}");
        type = new MinimaxRequestAdapter().getContentType(jsonObject);

        Assertions.assertEquals(ModelRespContentType.FINISH_SIGNAL, type);
    }

    @Test
    public void getContentTypeEndTypeTest() {
        JSONObject jsonObject = JSONObject.parseObject("{\"id\":\"050c61dc26892db2b086f74844bf29b5\",\""
            + "choices\":[{\"finish_reason\":\"stop\",\"index\":0,\"message\":{\"content\":\"你好呀，有什"
            + "么我可以帮你的吗？（面带微笑，语气亲切）\",\"role\":\"assistant\",\"name\":\"MM智能助理\",\"audio_con"
            + "tent\":\"\"}}],\"created\":1757228764,\"model\":\"abab6.5t-chat\",\"object\":\"chat.completion\",\""
            + "usage\":{\"total_tokens\":192,\"total_characters\":0,\"prompt_tokens\":178,\"completion_tokens\":14"
            + "},\"input_sensitive\":false,\"output_sensitive\":false,\"input_sensitive_type\":0,\"output_sen"
            + "sitive_type\":0,\"output_sensitive_int\":0,\"base_resp\":{\"status_code\":0,\"status_msg\":\"\"}}");
        ModelRespContentType type = new MinimaxRequestAdapter().getContentType(jsonObject);

        Assertions.assertEquals(ModelRespContentType.END_SIGNAL, type);

        jsonObject = JSONObject.parseObject("{\"id\":\"050c61dc26892db2b086f74844bf29b5\",\""
            + "choices\":[{\"finish_reason\":\"stop\",\"index\":0,\"message\":{\"content\":\"你好呀，有什"
            + "么我可以帮你的吗？（面带微笑，语气亲切）\",\"role\":\"assistant\",\"name\":\"MM智能助理\",\"audio_con"
            + "tent\":\"\"}}],\"created\":1757228764,\"model\":\"abab6.5t-chat\",\"object\":\"chat.completion\",\""
            + "usage\":{\"total_tokens\":192,\"total_characters\":0,\"prompt_tokens\":178,\"completion_tokens\":14"
            + "},\"input_sensitive\":false,\"output_sensitive\":false,\"input_sensitive_type\":0,\"output_sen"
            + "sitive_type\":0,\"output_sensitive_int\":0,\"base_resp\":{\"status_code\":1,\"status_msg\":\"\"}}");
        type = new MinimaxRequestAdapter().getContentType(jsonObject);
        Assertions.assertEquals(ModelRespContentType.ERR_SIGNAL, type);
    }

    @Test
    public void getFinishContentWithRequestIdTest() {
        JSONObject jsonObject = JSONObject.parseObject("{\"id\":\"050c61dc26892db2b086f74844bf29b5\",\""
            + "choices\":[{\"finish_reason\":\"stop\",\"index\":0,\"message\":{\"content\":\"你好呀，有什"
            + "么我可以帮你的吗？（面带微笑，语气亲切）\",\"role\":\"assistant\",\"name\":\"MM智能助理\",\"audio_con"
            + "tent\":\"\"}}],\"created\":1757228764,\"model\":\"abab6.5t-chat\",\"object\":\"chat.completion\",\""
            + "usage\":{\"total_tokens\":192,\"total_characters\":0,\"prompt_tokens\":178,\"completion_tokens\":14"
            + "},\"input_sensitive\":false,\"output_sensitive\":false,\"input_sensitive_type\":0,\"output_sen"
            + "sitive_type\":0,\"output_sensitive_int\":0,\"base_resp\":{\"status_code\":0,\"status_msg\":\"\"}}");

        String content = new MinimaxRequestAdapter().getFinishContentWithRequestId("test", null, new ModelApiLog());
        Assertions.assertEquals("test", content);

        content = new MinimaxRequestAdapter().getFinishContentWithRequestId("test", jsonObject, new ModelApiLog());

        Assertions.assertTrue(content.contains("\"finish_reason\":\"stop\""));
    }
}
