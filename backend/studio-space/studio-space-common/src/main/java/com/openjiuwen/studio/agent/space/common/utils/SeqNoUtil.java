/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils;

/**
 * 序列号服务
 */
public class SeqNoUtil {
    public interface ChatPrefix {
        String CHAT_FILE = "CF_";
    }

    public interface LoginLog {
        String LOGIN_LOG = "LL_";
    }

    public static String generateId(String prefix) {
        String planId = StringUtil.generateUUID(StringUtil.seqIdLength);
        return prefix + planId;
    }
}
