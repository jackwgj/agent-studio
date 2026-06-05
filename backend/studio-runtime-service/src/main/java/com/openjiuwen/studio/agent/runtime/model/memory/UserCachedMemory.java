/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.memory;

import com.openjiuwen.studio.agent.common.dto.agent.Message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户记忆缓存区 记忆存储模型
 *
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCachedMemory {
    private String userId;

    private String applicationId;

    private String applicationType;

    private Long lastestUpdateTimeMills;

    private List<UserCachedConversation> cachedConversations;

    public UserCachedMemory(String userId, String applicationId, String applicationType, String conversationId,
        UserCachedChatTurn chatTurn) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.applicationType = applicationType;
        this.lastestUpdateTimeMills = System.currentTimeMillis();

        List<UserCachedChatTurn> chatTurns = new ArrayList<>();
        chatTurns.add(chatTurn);

        UserCachedConversation userCachedConversation = new UserCachedConversation();
        userCachedConversation.setConversationId(conversationId);
        userCachedConversation.setChatTurns(chatTurns);

        this.cachedConversations = new ArrayList<>();
        this.cachedConversations.add(userCachedConversation);
    }

    public UserCachedMemory(String userId, String applicationId, String conversationId,
        List<UserCachedChatTurn> chatTurns) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.lastestUpdateTimeMills = System.currentTimeMillis();

        UserCachedConversation userCachedConversation = new UserCachedConversation();
        userCachedConversation.setConversationId(conversationId);
        userCachedConversation.setChatTurns(chatTurns);

        this.cachedConversations = new ArrayList<>();
        this.cachedConversations.add(userCachedConversation);
    }

    /**
     * 添加用户记忆缓冲区的对话轮次消息
     *
     * @param conversationId conversationId
     * @param chatTurn chatTurn
     */
    public void addChatTurn(String conversationId, UserCachedChatTurn chatTurn) {
        if (StringUtils.isBlank(conversationId) || chatTurn == null) {
            log.error("Failed to add chat turn: blank userId or charTurn.");
            return;
        }

        if (this.getCachedConversations() == null) {
            List<UserCachedChatTurn> chatTurns = new ArrayList<>();
            chatTurns.add(chatTurn);

            UserCachedConversation userCachedConversation = new UserCachedConversation();
            userCachedConversation.setConversationId(conversationId);
            userCachedConversation.setChatTurns(chatTurns);

            this.cachedConversations = new ArrayList<>();
            this.cachedConversations.add(userCachedConversation);
            this.lastestUpdateTimeMills = System.currentTimeMillis();

            return;
        }

        UserCachedConversation existConversation = null;
        for (UserCachedConversation cachedConversation : this.getCachedConversations()) {
            if (conversationId.equals(cachedConversation.getConversationId())) {
                existConversation = cachedConversation;
                break;
            }
        }

        if (existConversation == null) {
            List<UserCachedChatTurn> chatTurns = new ArrayList<>();
            chatTurns.add(chatTurn);

            UserCachedConversation userCachedConversation = new UserCachedConversation();
            userCachedConversation.setConversationId(conversationId);
            userCachedConversation.setChatTurns(chatTurns);

            this.cachedConversations.add(userCachedConversation);
        } else {
            existConversation.getChatTurns().add(chatTurn);
        }
        this.lastestUpdateTimeMills = System.currentTimeMillis();
    }

    /**
     * 获取缓冲区当前缓冲的所有消息列表
     *
     * @return List<Message>
     */
    public List<Message> getAllMessages() {
        if (CollectionUtils.isEmpty(this.getCachedConversations())) {
            return Collections.emptyList();
        }

        List<Message> messages = new ArrayList<>();
        for (UserCachedConversation cachedConversation : this.getCachedConversations()) {
            List<UserCachedChatTurn> chatTurns = cachedConversation.getChatTurns();
            if (CollectionUtils.isNotEmpty(chatTurns)) {
                for (UserCachedChatTurn chatTurn : chatTurns) {
                    if (CollectionUtils.isNotEmpty(chatTurn.getMessages())) {
                        messages.addAll(chatTurn.getMessages());
                    }
                }
            }
        }

        return messages;
    }

    /**
     * 返回当前已完成的对话轮数
     *
     * @return int
     */
    public int countFinishedChatTurn() {
        if (CollectionUtils.isEmpty(this.getCachedConversations())) {
            return 0;
        }

        int finishedChatTurn = 0;
        for (UserCachedConversation conversation : this.getCachedConversations()) {
            List<UserCachedChatTurn> chatTurns = conversation.getChatTurns();

            finishedChatTurn += CollectionUtils.size(chatTurns);
        }
        return finishedChatTurn;
    }
}
