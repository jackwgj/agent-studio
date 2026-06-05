/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志上下文
 */
public class LoggerContext {
    private static final ThreadLocal<Map<String, String>> MAP_LOCAL = ThreadLocal.withInitial(HashMap::new);

    public LoggerContext() {
    }

    public static String get(String key) {
        return (String) ((Map<?, ?>) MAP_LOCAL.get()).get(key);
    }

    public static void put(String key, String value) {
        MAP_LOCAL.get().put(key, value);
    }

    public static void removeKey(String key) {
        ((Map<?, ?>) MAP_LOCAL.get()).remove(key);
    }

    public static void clear() {
        MAP_LOCAL.get().clear();
    }

    public static void remove() {
        MAP_LOCAL.remove();
    }

    public static Map<?,?> copyAll() {
        return Collections.unmodifiableMap(MAP_LOCAL.get());
    }
}
