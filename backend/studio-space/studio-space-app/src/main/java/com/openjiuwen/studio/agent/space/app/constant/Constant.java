package com.openjiuwen.studio.agent.space.app.constant;

import java.util.List;

/**
 * 常量
 */
public class Constant {
    // MB
    public static final int MB = 1024 * 1024;

    //GB
    public static final int GB = 1024 * 1024 * 1024;

    // 是否流式接口
    public static final String STREAM_FIELD = "stream";

    // 存储taskId对应的agent状态的key 0为异常 1为正常
    public static final String AGENT_STATUS = "AGENT_STATUS_%s";

    public static final String DATABASE_NEXT_KEY = "DATABASE_NEXT_KEY_%s";

    public static final String MESSAGE_SNAPSHOT = "MESSAGE_SNAPSHOT_%s";

    // 调用agent重试次数
    public static final int AGENT_RETRY_TIME = 3;

    // 限制单个定时任务单次处理的任务数量
    public static final int AGENT_TASK_HANDLER_COUNT = 60;

    // 内置AgentId
    public static final List<String> INNER_AGENT_ID_LIST = List.of("operator_agent", "college_entrance_examinations_agent",
        "college_entrance", "travel_plan", "deepresearch", "game_html", "market_research", "contract_review", "procurement_research");

    public static final String DEFAULT_AGENT = "operator_agent";

    public static final Long REDIS_EXPIRE_TIME = 14400L;

    public static final String SPACE_OBS_BUCKET_NAME = "AGENT_BUILDER_SPACE_OBS_BUCKET_NAME";

    public static final String SPACE_OBS_DIRECTORY_TMP = "SPACE_OBS_DIRECTORY_TMP";

    public static final String SPACE_OBS_DIRECTORY_RUNTIME = "SPACE_OBS_DIRECTORY_RUNTIME";

    public static final String SPACE_OBS_TEMP_DIRECTORY_PREFIX = "USER_UPLOAD_";

    public static final String SPACE_OBS_DOMAIN_ID_PREFIX = "DOMAIN_";

    public static final String SPACE_OBS_TASK_ID_PREFIX = "TASK_";

    public final static String AI_3_IMAGE_TYPE = "AIAPP_BASE_IMAGE_V3";

    public static final String DONE = "[DONE]";

    public static final String READY_EVENT = "ready";

    public static final int EVENT_ID_LEN = 32;

    public static final String MESSAGE = "message";

    public static final String ERROR = "error";

    public static final String CONVERSATION_ID = "conversationId";

    public static final String REQUEST_MSG_ID = "requestMsgId";

    public static final String RESPONSE_MSG_ID = "responseMsgId";

    /**
     * 应用管理中的agent状态  active上线，inactive下线
     */
    public static final String AGENT_STATUS_ACTIVE = "active";
    public static final String AGENT_STATUS_INACTIVE = "inactive";

}
