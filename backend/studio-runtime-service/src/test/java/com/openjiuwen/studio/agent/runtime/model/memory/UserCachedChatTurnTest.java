/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.agent.common.dto.agent.Message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UserCachedChatTurnTest {
    private Message addMessage;

    private static final String TEST_ADD_MESSAGE = "message";

    @BeforeEach
    public void setup() {
        addMessage = new Message();
        addMessage.setContent(TEST_ADD_MESSAGE);
    }

    @Test
    void should_do_nothing_when_add_message_using_null_message() {
        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn();
        userCachedChatTurn.addMessage(null);

        assertNull(userCachedChatTurn.getMessages());
        assertEquals(0, userCachedChatTurn.getLastUpdateTime());
    }

    @Test
    void should_add_message_success_when_using_single_valid_message() {
        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn();
        userCachedChatTurn.addMessage(addMessage);

        assertNotNull(userCachedChatTurn.getMessages());
        assertEquals(1, userCachedChatTurn.getMessages().size());
        assertEquals(addMessage, userCachedChatTurn.getMessages().get(0));
    }

    @Test
    void should_success_when_add_message_to_not_empty_chat_turn() {
        Message existentMessage = new Message();
        existentMessage.setContent("existentMessage");

        List<Message> existentMessages = new ArrayList<>();
        existentMessages.add(existentMessage);

        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn(existentMessages);
        userCachedChatTurn.addMessage(addMessage);

        assertNotNull(userCachedChatTurn.getMessages());
        assertEquals(2, userCachedChatTurn.getMessages().size());
        assertEquals(addMessage, userCachedChatTurn.getMessages().get(1));
    }

    @Test
    void should_do_nothing_when_add_messages_using_null_message() {
        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn();
        userCachedChatTurn.addMessages(null);

        assertNull(userCachedChatTurn.getMessages());
        assertEquals(0, userCachedChatTurn.getLastUpdateTime());

        userCachedChatTurn.addMessages(Collections.emptyList());

        assertNull(userCachedChatTurn.getMessages());
        assertEquals(0, userCachedChatTurn.getLastUpdateTime());
    }

    @Test
    void should_add_messages_success_when_using_single_valid_message() {
        List<Message> addMessages = new ArrayList<>();
        addMessages.add(addMessage);

        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn();
        userCachedChatTurn.addMessages(addMessages);

        assertNotNull(userCachedChatTurn.getMessages());
        assertEquals(1, userCachedChatTurn.getMessages().size());
        assertEquals(addMessage, userCachedChatTurn.getMessages().get(0));
    }

    @Test
    void should_success_when_add_messages_to_not_empty_chat_turn() {
        Message existentMessage = new Message();
        existentMessage.setContent("existentMessage");

        List<Message> existentMessages = new ArrayList<>();
        existentMessages.add(existentMessage);

        UserCachedChatTurn userCachedChatTurn = new UserCachedChatTurn(existentMessages);
        userCachedChatTurn.addMessages(Collections.singletonList(addMessage));

        assertNotNull(userCachedChatTurn.getMessages());
        assertEquals(2, userCachedChatTurn.getMessages().size());
        assertEquals(addMessage, userCachedChatTurn.getMessages().get(1));
    }
}
