package com.openjiuwen.studio.conversation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationWorkflowNode {
    private String conversationId;
    private String toolId;
    private String parentRunId;
    private String workflowId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Integer nodeIndex;
    private String status;
    private String inputContent;
    private String outputContent;
    private String errorCode;
    private String errorMessage;
    private Date startedOn;
    private Date finishedOn;
    private String projectId;
    private String workspaceId;
    private String domainId;
    private String creatorId;
}
