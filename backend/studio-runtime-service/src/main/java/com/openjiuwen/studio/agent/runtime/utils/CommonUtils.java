/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 常用工具类
 *
 */
public class CommonUtils {

    /**
     * list截取函数
     *
     * @param offset start
     * @param limit 限制长度
     * @param insightInfo insightInfo
     * @return 切割后list
     */
    public static <T> List<T> subList(Integer offset, Integer limit, List<T> insightInfo) {
        // offset和limit默认值为0，如果offset小于0，则默认为0；
        if (offset == null || offset < 0) {
            offset = 0;
        }
        // 如果limit小于0，则默认为0；
        if (limit == null || limit < 0) {
            limit = insightInfo.size();
        }
        // 如果offset+limit大于列表长度，则limit为剩余长度
        if (offset + limit > insightInfo.size()) {
            limit = insightInfo.size() - offset;
        }
        if (offset >= insightInfo.size() || limit <= 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(insightInfo.subList(offset, offset + limit));
    }

    /**
     * 分组
     *
     * @param list 原始范围
     * @param groupSize 分组大小
     * @param <T> 类型
     * @return 分组后数据
     */
    public static <T> List<List<T>> groupList(List<T> list, int groupSize) {
        return IntStream.range(0, (list.size() + groupSize - 1) / groupSize)
            .mapToObj(i -> list.subList(i * groupSize, Math.min((i + 1) * groupSize, list.size())))
            .collect(Collectors.toList());
    }

    /**
     * inputStream转化为byte数组
     *
     * @param inputStream 输入流
     * @return byte[]
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        // 读取 InputStream 并写入 ByteArrayOutputStream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    public static String toTraceModel(String defaultMode, String invokeMode) {
        if (StringUtils.isEmpty(invokeMode)) {
            return defaultMode;
        }
        return switch (invokeMode.toUpperCase(Locale.ROOT)) {
            case "DEBUG" -> "DEBUG";
            case "EXPERIMENT" -> "EXPERIMENT";
            case "PROD", "API" -> "API";
            default -> defaultMode;
        };
    }

    public static boolean traceEnable(boolean opsEnable, String mode) {
        if (!opsEnable) {
            return false;
        }
        return !StringUtils.isEmpty(mode);
    }
}
