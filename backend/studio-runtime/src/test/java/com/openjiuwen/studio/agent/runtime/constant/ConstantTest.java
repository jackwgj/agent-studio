/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.constant;

import java.util.Date;

public interface ConstantTest {
    String TEST_PROJECT_ID = "test_project_id";

    String TEST_DOMAIN_ID = "test_domain_id";

    String TEST_TOKEN = "test_x_auth_token";

    String TEST_USER_ID = "test_user_id";

    String TEST_EXECUTION_ID = "test_execution_id";

    String TEST_PLUGIN_ID = "test_plugin_id";

    String TEST_PRESET_PLUGIN_ID = "preset_plugin_id";

    String TEST_AGENT_ID = "test_agent_id";

    String TEST_AGENT_NAME = "test_agent_name";

    String TEST_AGENT_QUESTION = "test_agent_question";

    String TEST_ASSISTANT_ANSWER = "test_assistant_answer";

    String IR_MD5 = "test_id_md5";

    String TEST_TRIGGER_ID = "test_trigger_id";

    String TEST_KNOWLEDGE_REPO_ID = "test_repo_id";

    Date TEST_UPDATED_TIME = new Date();

    String TEST_MODEL_DEPLOYMENT_ID = "test_model_deployment_id";

    Long TEST_MESSAGE_ID = 1750171051023L;

    interface Workflow {
        String TEST_LLM_WORKFLOW_ID = "test_llm_workflow_id";

        String TEST_TWQ_WORKFLOW_ID = "test_twq_workflow_id";
    }

    interface Conversation {
        String TEST_CONVERSATION_ID = "test-user-conversation-123";

        String TEST_CONVERSATION_ID_FAILED = "test-user-conversation-failed";

        String TEST_INSIGHT_CONVERSATION_ID = "test-user-conversation-insight-123";

        String TEST_TWQ_CONVERSATION_ID = "test-user-conversation-twq-123";

        String TEST_MULTI_AGENT_CONVERSATION_ID = "test-run-agent-with-multi-agent-conversation-id";
    }
}
