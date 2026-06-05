package com.openjiuwen.studio.agent.space.common.utils;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;

@Slf4j
public class LogUtils {
    public static final int MSG_MAX_LENGTH = 1000;

    public static String encodeForLog(Object obj) {
        if (obj == null) {
            return "null";
        }
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
