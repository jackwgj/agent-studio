/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestUtil {
    public static String getStringFromFile(String resourceLocation) {
        try {
            return FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8).replace(
                "\r\n", "\n");
        } catch (IOException e) {
            log.error("parse File failed", e);
            return "";
        }
    }

    public static <T> T getJsonObject(Class<T> clazz, String resourceLocation) throws IOException {
        final String body = FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8);
        return JSONObject.parseObject(body, clazz);
    }
}
