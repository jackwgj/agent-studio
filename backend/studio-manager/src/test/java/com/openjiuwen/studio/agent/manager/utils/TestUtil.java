/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 测试Util类
 *
 */
@Slf4j
public class TestUtil {
    public static String getStringFromFile(String resourceLocation) {
        try {
            return FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        } catch (IOException e) {
            log.error("parse File failed", e);
            return "";
        }
    }

    public static <T> T getJsonObject(Class<T> clazz, String resourceLocation) throws IOException {
        final String body = FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8);
        return JsonUtils.json2ObjQuietly(body, clazz);
    }

    public static <E> List<E> getJsonArray(Class<E> clazz, String resourceLocation) throws IOException {
        final String body = FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8);
        return JsonUtils.json2Array(body, clazz);
    }

    public static void logJson(Object json) {
        log.info("\n" + JSON.toJSONString(json, true));
    }

    public static String prettyJson(Object obj) {
        if (obj instanceof String) {
            obj = JSONObject.parseObject(obj.toString());
        }
        return JSONObject.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
    }

    public static String prettyJsonFile(String resourceLocation, boolean isArray) {
        String content = TestUtil.getStringFromFile(resourceLocation);
        if (isArray) {
            JSONArray array = JSONArray.parseArray(content);
            return JSONArray.toJSONString(array, JSONWriter.Feature.PrettyFormat);
        } else {
            JSONObject obj = JSONObject.parseObject(content);
            return JSONObject.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
        }
    }

    public static String prettyJsonFile(String resourceLocation) {
        // 默认非数组
        return prettyJsonFile(resourceLocation, false);
    }

    public static void assertResAndObjEquals(String resourceLocation, Object obj) {
        Assertions.assertEquals(TestUtil.prettyJsonFile(resourceLocation, false), TestUtil.prettyJson(obj));
    }

    @SuppressWarnings("rawtypes")
    public static void assertResAndObjEquals(String resourceLocation, List list) {
        Assertions.assertEquals(TestUtil.prettyJsonFile(resourceLocation, true), TestUtil.prettyJson(list));
    }
}
