package com.openjiuwen.studio.conversation.application.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SendMessageCmdTest {

    @Test
    void testData_SetsAndGetsAllFields() {
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("你好");
        cmd.setModelDeploymentId("deploy-001");

        assertNotNull(cmd);
        assertEquals("你好", cmd.getQuery());
        assertEquals("deploy-001", cmd.getModelDeploymentId());
    }

    @Test
    void testSerialization_UsesJsonPropertySnakeCase() throws JsonProcessingException {
        SendMessageCmd cmd = new SendMessageCmd();
        cmd.setQuery("你好");
        cmd.setModelDeploymentId("deploy-001");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(cmd);
        JsonNode tree = mapper.readTree(json);

        // 对外契约：JSON 键是下划线，不是驼峰
        assertTrue(tree.has("model_deployment_id"));
        assertFalse(tree.has("modelDeploymentId"));
    }

    @Test
    void testDeserialization_AcceptsSnakeCaseJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SendMessageCmd back = mapper.readValue(
                "{\"query\":\"你好\",\"model_deployment_id\":\"deploy-001\"}", SendMessageCmd.class);

        assertEquals("你好", back.getQuery());
        assertEquals("deploy-001", back.getModelDeploymentId());
    }
}
