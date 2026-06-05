/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 共用辅助类
 *
 */
@Slf4j
public class CommonUtils {
    private CommonUtils() {
    }

    /**
     * 读取resource下资源文件内容
     */
    public static String getResourceContent(String resourceName) {
        if (StringUtils.isEmpty(resourceName)) {
            return StringUtils.EMPTY;
        }

        try (InputStream inputStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                log.warn("Resource [{}] not found, return empty string", resourceName);
                return StringUtils.EMPTY;
            }
            try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                return Strings.CS.removeEnd(buffer.toString(), "\n");
            } catch (IOException e) {
                log.error("resource reader error.", e);
                throw new AgentStudioException(StudioError.RESOURCE_READER_ERROR);
            }
        } catch (Exception e) {
            log.error("Read resource [{}] error: {}", resourceName, e);
            return StringUtils.EMPTY;
        }
    }
}
