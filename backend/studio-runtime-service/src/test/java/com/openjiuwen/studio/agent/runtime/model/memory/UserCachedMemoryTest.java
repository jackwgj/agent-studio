/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.agent.common.dto.agent.Message;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class UserCachedMemoryTest {
    private UserCachedMemory userCachedMemory;

    private UserCachedChatTurn userCachedChatTurn;

    private static final String TEST_USER_ID = "userId";

    private static final String TEST_CONVERSATION_ID = "conversationId";

    private static final String TEST_ADDED_MESSAGE_CONTENT = "addedContent";

    private static final String EXISTENT_CONVERSATION_ID = "existentConversation";

    @BeforeEach
    void setup() {
        userCachedMemory = new UserCachedMemory();
        userCachedMemory.setUserId(TEST_USER_ID);

        Message addedMessage = new Message();
        addedMessage.setContent(TEST_ADDED_MESSAGE_CONTENT);

        List<Message> messages = new ArrayList<>();
        messages.add(addedMessage);
        userCachedChatTurn = new UserCachedChatTurn(messages);
    }

    @Test
    void should_do_nothing_when_add_chat_turn_using_empty_conversation_id() {
        userCachedMemory.addChatTurn(StringUtils.EMPTY, userCachedChatTurn);
        assertNull(userCachedMemory.getLastestUpdateTimeMills());
        assertNull(userCachedMemory.getCachedConversations());

        assertEquals(0, userCachedMemory.countFinishedChatTurn());
    }

    @Test
    void should_do_nothing_when_add_chat_turn_using_null_turn() {
        userCachedMemory.addChatTurn(TEST_CONVERSATION_ID, null);
        assertNull(userCachedMemory.getLastestUpdateTimeMills());
        assertNull(userCachedMemory.getCachedConversations());

        assertEquals(0, userCachedMemory.countFinishedChatTurn());
    }

    @Test
    void should_add_conversation_when_add_chat_turn_while_memeory_conversation_is_null() {
        userCachedMemory.addChatTurn(TEST_CONVERSATION_ID, userCachedChatTurn);

        assertNotNull(userCachedMemory.getLastestUpdateTimeMills());
        assertNotNull(userCachedMemory.getCachedConversations());

        List<UserCachedConversation> userCachedConversations = userCachedMemory.getCachedConversations();
        assertNotNull(userCachedConversations);
        assertEquals(1, userCachedConversations.size());

        List<UserCachedChatTurn> userCachedChatTurns = userCachedConversations.get(0).getChatTurns();
        assertNotNull(userCachedChatTurns);
        assertEquals(1, userCachedChatTurns.size());
        assertEquals(userCachedChatTurn, userCachedChatTurns.get(0));

        assertEquals(1, userCachedMemory.countFinishedChatTurn());
    }

    @Test
    void should_append_chat_turn_when_add_chat_turn_with_existent_conversation() {
        Message existMessage = new Message();
        existMessage.setContent("EXISTENT_MESSAGE_CONTENT");
        List<Message> existMessages = new ArrayList<>();
        existMessages.add(existMessage);
        UserCachedChatTurn existUserCachedChatTurn = new UserCachedChatTurn(existMessages);

        List<UserCachedChatTurn> existUserCachedChatTurns = new ArrayList<>();
        existUserCachedChatTurns.add(existUserCachedChatTurn);

        UserCachedConversation existentUserCachedConversation = new UserCachedConversation();
        existentUserCachedConversation.setConversationId(TEST_CONVERSATION_ID);
        existentUserCachedConversation.setChatTurns(existUserCachedChatTurns);

        List<UserCachedConversation> existCachedConversations = new ArrayList<>();
        existCachedConversations.add(existentUserCachedConversation);
        userCachedMemory.setCachedConversations(existCachedConversations);

        userCachedMemory.addChatTurn(TEST_CONVERSATION_ID, userCachedChatTurn);

        assertNotNull(userCachedMemory.getLastestUpdateTimeMills());

        List<UserCachedConversation> userCachedConversations = userCachedMemory.getCachedConversations();
        assertNotNull(userCachedConversations);
        assertEquals(1, userCachedConversations.size());

        UserCachedConversation resultConversation = userCachedConversations.get(0);
        assertNotNull(resultConversation);
        assertEquals(TEST_CONVERSATION_ID, resultConversation.getConversationId());

        List<UserCachedChatTurn> resultChatTurns = resultConversation.getChatTurns();
        assertNotNull(resultChatTurns);
        assertEquals(2, resultChatTurns.size());
        assertEquals(existUserCachedChatTurn, resultChatTurns.get(0));
        assertEquals(userCachedChatTurn, resultChatTurns.get(1));

        assertEquals(2, userCachedMemory.countFinishedChatTurn());
    }

    @Test
    void should_create_new_conversation_when_add_chat_turn_with_nonexistent_conversation() {
        Message existMessage = new Message();
        existMessage.setContent("EXISTENT_MESSAGE_CONTENT");
        List<Message> existMessages = new ArrayList<>();
        existMessages.add(existMessage);
        UserCachedChatTurn existUserCachedChatTurn = new UserCachedChatTurn(existMessages);

        List<UserCachedChatTurn> existUserCachedChatTurns = new ArrayList<>();
        existUserCachedChatTurns.add(existUserCachedChatTurn);

        UserCachedConversation existentUserCachedConversation = new UserCachedConversation();
        existentUserCachedConversation.setConversationId(EXISTENT_CONVERSATION_ID);
        existentUserCachedConversation.setChatTurns(existUserCachedChatTurns);

        List<UserCachedConversation> existCachedConversations = new ArrayList<>();
        existCachedConversations.add(existentUserCachedConversation);
        userCachedMemory.setCachedConversations(existCachedConversations);

        userCachedMemory.addChatTurn(TEST_CONVERSATION_ID, userCachedChatTurn);

        assertNotNull(userCachedMemory.getLastestUpdateTimeMills());

        List<UserCachedConversation> userCachedConversations = userCachedMemory.getCachedConversations();
        assertNotNull(userCachedConversations);
        assertEquals(2, userCachedConversations.size());

        assertEquals(existentUserCachedConversation, userCachedConversations.get(0));

        UserCachedConversation addedConversation = userCachedConversations.get(1);
        assertNotNull(addedConversation);
        assertEquals(TEST_CONVERSATION_ID, addedConversation.getConversationId());

        List<UserCachedChatTurn> resultChatTurns = addedConversation.getChatTurns();
        assertNotNull(resultChatTurns);
        assertEquals(1, resultChatTurns.size());
        assertEquals(userCachedChatTurn, resultChatTurns.get(0));

        assertEquals(2, userCachedMemory.countFinishedChatTurn());
    }
}
