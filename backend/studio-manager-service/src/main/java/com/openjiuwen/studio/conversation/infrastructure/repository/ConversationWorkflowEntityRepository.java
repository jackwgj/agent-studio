package com.openjiuwen.studio.conversation.infrastructure.repository;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationWorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationWorkflowEntityRepository extends JpaRepository<ConversationWorkflowEntity, Long> {
}
