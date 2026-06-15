/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ShowMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryRepoDtoTest {

    @Test
    void testCreateRequestBody_conversationRound_and_timeSpan() {
        CreateMemoryRepoRequestBody body = new CreateMemoryRepoRequestBody();
        body.setName("test");
        body.setConversationRound(5);
        body.setTimeSpan(30);

        assertEquals(5, body.getConversationRound());
        assertEquals(30, body.getTimeSpan());
    }

    @Test
    void testCreateRequestBody_null_defaults() {
        CreateMemoryRepoRequestBody body = new CreateMemoryRepoRequestBody();

        assertNull(body.getConversationRound());
        assertNull(body.getTimeSpan());
    }

    @Test
    void testModifyRequestBody_conversationRound_and_timeSpan() {
        ModifyMemoryRepoRequestBody body = new ModifyMemoryRepoRequestBody();
        body.setName("test");
        body.setConversationRound(10);
        body.setTimeSpan(60);

        assertEquals(10, body.getConversationRound());
        assertEquals(60, body.getTimeSpan());
    }

    @Test
    void testModifyRequestBody_null_defaults() {
        ModifyMemoryRepoRequestBody body = new ModifyMemoryRepoRequestBody();

        assertNull(body.getConversationRound());
        assertNull(body.getTimeSpan());
    }

    @Test
    void testShowResponseBody_conversationRound_and_timeSpan() {
        ShowMemoryRepoResponseBody body = new ShowMemoryRepoResponseBody();
        body.setMemoryRepoId("repo-1");
        body.setConversationRound(3);
        body.setTimeSpan(15);

        assertEquals("repo-1", body.getMemoryRepoId());
        assertEquals(3, body.getConversationRound());
        assertEquals(15, body.getTimeSpan());
    }

    @Test
    void testShowResponseBody_null_defaults() {
        ShowMemoryRepoResponseBody body = new ShowMemoryRepoResponseBody();

        assertNull(body.getConversationRound());
        assertNull(body.getTimeSpan());
    }

    @Test
    void testCreateRequestBody_equals_with_new_fields() {
        CreateMemoryRepoRequestBody body1 = new CreateMemoryRepoRequestBody();
        body1.setName("test");
        body1.setConversationRound(5);
        body1.setTimeSpan(10);

        CreateMemoryRepoRequestBody body2 = new CreateMemoryRepoRequestBody();
        body2.setName("test");
        body2.setConversationRound(5);
        body2.setTimeSpan(10);

        assertEquals(body1, body2);
        assertEquals(body1.hashCode(), body2.hashCode());
    }

    @Test
    void testModifyRequestBody_equals_with_new_fields() {
        ModifyMemoryRepoRequestBody body1 = new ModifyMemoryRepoRequestBody();
        body1.setName("test");
        body1.setConversationRound(5);
        body1.setTimeSpan(10);

        ModifyMemoryRepoRequestBody body2 = new ModifyMemoryRepoRequestBody();
        body2.setName("test");
        body2.setConversationRound(5);
        body2.setTimeSpan(10);

        assertEquals(body1, body2);
        assertEquals(body1.hashCode(), body2.hashCode());
    }

    @Test
    void testShowResponseBody_equals_with_new_fields() {
        ShowMemoryRepoResponseBody body1 = new ShowMemoryRepoResponseBody();
        body1.setMemoryRepoId("repo-1");
        body1.setConversationRound(3);
        body1.setTimeSpan(15);

        ShowMemoryRepoResponseBody body2 = new ShowMemoryRepoResponseBody();
        body2.setMemoryRepoId("repo-1");
        body2.setConversationRound(3);
        body2.setTimeSpan(15);

        assertEquals(body1, body2);
        assertEquals(body1.hashCode(), body2.hashCode());
    }

    @Test
    void testCreateRequestBody_toString_includes_new_fields() {
        CreateMemoryRepoRequestBody body = new CreateMemoryRepoRequestBody();
        body.setName("test");
        body.setConversationRound(5);
        body.setTimeSpan(10);

        String str = body.toString();
        assert str.contains("conversationRound");
        assert str.contains("timeSpan");
    }

    @Test
    void testShowResponseBody_toString_includes_new_fields() {
        ShowMemoryRepoResponseBody body = new ShowMemoryRepoResponseBody();
        body.setMemoryRepoId("repo-1");
        body.setConversationRound(3);
        body.setTimeSpan(15);

        String str = body.toString();
        assert str.contains("conversationRound");
        assert str.contains("timeSpan");
    }

    @Test
    void testModifyRequestBody_strategies_accepts_up_to_200() {
        ModifyMemoryRepoRequestBody body = new ModifyMemoryRepoRequestBody();
        body.setName("test");

        // Build 200 strategies — should be within the @Size(max=200) limit
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            LongTermMemoryStrategy s = new LongTermMemoryStrategy();
            s.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
            s.setPrompt("prompt " + i);
            strategies.add(s);
        }
        body.setLongTermMemoryStrategies(strategies);

        assertEquals(200, body.getLongTermMemoryStrategies().size());
    }

    @Test
    void testModifyRequestBody_strategies_validation_max_200() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ModifyMemoryRepoRequestBody body = new ModifyMemoryRepoRequestBody();
        body.setName("test");

        // Build 201 strategies — should violate @Size(max=200)
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            LongTermMemoryStrategy s = new LongTermMemoryStrategy();
            s.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
            s.setPrompt("prompt " + i);
            strategies.add(s);
        }
        body.setLongTermMemoryStrategies(strategies);

        Set<ConstraintViolation<ModifyMemoryRepoRequestBody>> violations = validator.validate(body);
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("longTermMemoryStrategies")),
            "Should have validation violation on longTermMemoryStrategies");

        factory.close();
    }

    @Test
    void testCreateRequestBody_strategies_validation_max_200() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        CreateMemoryRepoRequestBody body = new CreateMemoryRepoRequestBody();
        body.setName("test");

        // Build 201 strategies — should violate @Size(max=200) on Create too
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            LongTermMemoryStrategy s = new LongTermMemoryStrategy();
            s.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
            s.setPrompt("prompt " + i);
            strategies.add(s);
        }
        body.setLongTermMemoryStrategies(strategies);

        Set<ConstraintViolation<CreateMemoryRepoRequestBody>> violations = validator.validate(body);
        assertTrue(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("longTermMemoryStrategies")),
            "Create body should also have max=200 on longTermMemoryStrategies");

        factory.close();
    }
}
