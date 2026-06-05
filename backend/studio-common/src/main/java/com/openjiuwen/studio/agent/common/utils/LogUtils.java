/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.alibaba.fastjson.JSON;

import java.text.Normalizer;

/**
 * 功能描述
 *
 */
public class LogUtils {

    public static final int MSG_MAX_LENGTH = 1000;

    public LogUtils() {
    }

    public static String encodeForLog(Object obj) {
        if (obj == null) {
            return "null";
        } else {
            String msg;
            if (obj instanceof String) {
                msg = (String) obj;
            } else {
                msg = JSON.toJSONString(obj);
            }

            msg = msg.substring(0, Math.min(msg.length(), MSG_MAX_LENGTH));
            return Normalizer.normalize(msg, Normalizer.Form.NFKC).replaceAll("\r", "_").replaceAll("\n", "_");
        }
    }
}
