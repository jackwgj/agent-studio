/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务支持的文件类型
 */
public enum AgentBuilderFileType {
    // --- 常规文件 ---
    PDF("pdf", Set.of(AgentBuilderDataType.TEXT)),
    MD("md", Set.of(AgentBuilderDataType.TEXT)),
    TXT("txt", Set.of(AgentBuilderDataType.TEXT)),
    LOG("log", Set.of(AgentBuilderDataType.TEXT)),
    CSV("csv", Set.of(AgentBuilderDataType.TEXT)),
    XLSX("xlsx", Set.of(AgentBuilderDataType.TEXT)),
    DOCX("docx", Set.of(AgentBuilderDataType.TEXT)),
    PPTX("pptx", Set.of(AgentBuilderDataType.TEXT)),
    JSON("json", Set.of(AgentBuilderDataType.TEXT)),
    HTML("html", Set.of(AgentBuilderDataType.TEXT)),
    XML("xml", Set.of(AgentBuilderDataType.TEXT)),
    // --- 代码文件 ---
    PY("py", Set.of(AgentBuilderDataType.TEXT)),
    SH("sh", Set.of(AgentBuilderDataType.TEXT)),
    JS("js", Set.of(AgentBuilderDataType.TEXT)),
    JSX("jsx", Set.of(AgentBuilderDataType.TEXT)),
    // --- 图片文件 ---
    JPG("jpg", Set.of(AgentBuilderDataType.IMAGE)),
    JPEG("jpeg", Set.of(AgentBuilderDataType.IMAGE)),
    PNG("png", Set.of(AgentBuilderDataType.IMAGE)),
    ;

    @Getter
    private final String value;

    @Getter
    private final Set<AgentBuilderDataType> belongDataTypes;

    AgentBuilderFileType(String value, Set<AgentBuilderDataType> belongDataTypes) {
        this.value = value;
        this.belongDataTypes = belongDataTypes;
    }

    public static Set<String> getFileTypesOfDataType(AgentBuilderDataType dataType) {
        return Arrays.stream(AgentBuilderFileType.values())
            .filter(kdsFileType -> kdsFileType.getBelongDataTypes().contains(dataType))
            .map(AgentBuilderFileType::getValue)
            .collect(Collectors.toSet());
    }

    public static AgentBuilderFileType toAgentBuilderFileType(String fileSuffix) {
        for (AgentBuilderFileType value : values()) {
            if (value.getValue().equalsIgnoreCase(fileSuffix)) {
                return value;
            }
        }
        return null;
    }
}