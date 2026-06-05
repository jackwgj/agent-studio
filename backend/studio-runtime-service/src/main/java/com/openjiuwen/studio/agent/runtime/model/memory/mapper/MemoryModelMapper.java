/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.memory.mapper;

import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.runtime.model.memory.UserProfileTag;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.MemoryExtractMessageRole;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.MemoryMessage;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.TagValue;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MemoryModelMapper
 *
 */
@Mapper
public interface MemoryModelMapper {
    /**
     * INSTANCE
     */
    MemoryModelMapper INSTANCE = Mappers.getMapper(MemoryModelMapper.class);

    /**
     * toMemoryMessages
     *
     * @param messages messages
     * @param conversationId conversationId
     * @return List<MemoryMessage>
     */
    default List<MemoryMessage> toMemoryMessages(List<Message> messages, String conversationId) {
        return messages.stream().map(message -> toMemoryMessage(message, conversationId)).toList();
    }

    /**
     * toMemoryMessage
     *
     * @param message message
     * @param conversationId conversationId
     * @return MemoryMessage
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "message.role")
    @Mapping(target = "timestamp", source = "message.createTime")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "conversationId", source = "conversationId")
    MemoryMessage toMemoryMessage(Message message, String conversationId);

    default MemoryExtractMessageRole messageRoleConverter(String role) {
        return MemoryExtractMessageRole.fromValue(role);
    }

    /**
     * toUserProfileTags
     *
     * @param tagValues tagValues
     * @return List<UserProfileTag>
     */
    List<UserProfileTag> toUserProfileTags(List<TagValue> tagValues);

    /**
     * 将TagValue对象转换为UserProfileTag对象
     *
     * @param tagValue tagValue
     * @return UserProfileTag
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "topic", source = "topic")
    UserProfileTag toUserProfileTag(TagValue tagValue);
}
