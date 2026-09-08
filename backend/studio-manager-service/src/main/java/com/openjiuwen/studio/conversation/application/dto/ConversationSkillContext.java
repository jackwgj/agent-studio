/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application.dto;

import lombok.Getter;

import java.util.List;

@Getter
public final class ConversationSkillContext {
    private final List<ConversationSkillDescriptor> catalog;
    private final List<String> recommendedSkillIds;
    private final List<String> agentBoundSkillIds;

    public ConversationSkillContext(List<ConversationSkillDescriptor> catalog, List<String> recommendedSkillIds) {
        this(catalog, recommendedSkillIds, List.of());
    }

    public ConversationSkillContext(List<ConversationSkillDescriptor> catalog, List<String> recommendedSkillIds,
                                    List<String> agentBoundSkillIds) {
        this.catalog = List.copyOf(catalog);
        this.recommendedSkillIds = List.copyOf(recommendedSkillIds);
        this.agentBoundSkillIds = List.copyOf(agentBoundSkillIds);
    }

    public static ConversationSkillContext empty() {
        return new ConversationSkillContext(List.of(), List.of(), List.of());
    }
}
