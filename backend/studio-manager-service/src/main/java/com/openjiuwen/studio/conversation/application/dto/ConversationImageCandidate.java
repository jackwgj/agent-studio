/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application.dto;

import com.openjiuwen.studio.conversation.domain.model.valueobject.FileRef;

/** Server-derived attachment reference and its position in the conversation. */
public record ConversationImageCandidate(FileRef ref, boolean current, int ordinal) {
}
