package com.openjiuwen.studio.agent.space.app.constant;

public class RedisKeyConstant {
    public static final String REDIS_KEY_PREFIX = "SPACE:";

    public static final String DR_TEMPLATE = REDIS_KEY_PREFIX + "DR:TEMPLATE_%s";

    public static final String DR_STATUS = REDIS_KEY_PREFIX + "DR:STATUS_%s";

    public static final String DR_SSE_LOCK = REDIS_KEY_PREFIX + "DR:SSE_LOCK_%s";

    public static final String DR_CHUNKS = REDIS_KEY_PREFIX + "DR:CHUNKS_%s";
}
