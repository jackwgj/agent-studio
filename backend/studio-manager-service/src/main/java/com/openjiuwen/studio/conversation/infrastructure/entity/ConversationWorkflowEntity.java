/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_conversation_workflow")
public class ConversationWorkflowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("conversation_id") private String conversationId;
    @JsonProperty("tool_id") private String toolId;
    @JsonProperty("parent_run_id") private String parentRunId;
    @JsonProperty("workflow_id") private String workflowId;
    @JsonProperty("node_id") private String nodeId;
    @JsonProperty("node_name") private String nodeName;
    @JsonProperty("node_type") private String nodeType;
    @JsonProperty("node_index") private Integer nodeIndex;
    private String status;
    @JsonProperty("input_content") private String inputContent;
    @JsonProperty("output_content") private String outputContent;
    @JsonProperty("error_code") private String errorCode;
    @JsonProperty("error_message") private String errorMessage;
    @JsonProperty("started_on") private Date startedOn;
    @JsonProperty("finished_on") private Date finishedOn;
    @JsonProperty("project_id") private String projectId;
    @JsonProperty("workspace_id") private String workspaceId;
    @JsonProperty("domain_id") private String domainId;
    @JsonProperty("creator_id") private String creatorId;
    @JsonProperty("created_on") private Date createdOn;
    @JsonProperty("updated_on") private Date updatedOn;
    private Integer deleted;
}
