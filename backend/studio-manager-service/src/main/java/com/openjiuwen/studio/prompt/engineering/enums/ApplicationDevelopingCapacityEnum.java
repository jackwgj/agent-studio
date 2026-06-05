/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

import static com.openjiuwen.studio.prompt.engineering.enums.ApplicationDevelopingResourceEnum.BASIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 功能描述 应用开发功能项枚举
 *
 */
public enum ApplicationDevelopingCapacityEnum {

    /**
     * 提示词用例管理
     */
    PROMPT_CASE_MGMT("prompt_case_mgmt", Arrays.asList(BASIC)),

    /**
     * 提示词工程管理
     */
    PROMPT_ENGINEERING_MGMT("prompt_engineering_mgmt", Arrays.asList(BASIC)),

    /**
     * 提示词管理
     */
    PROMPT_MGMT("prompt_mgmt", Arrays.asList(BASIC));

    private final String name;

    private final List<ApplicationDevelopingResourceEnum> resources;

    ApplicationDevelopingCapacityEnum(String name, List<ApplicationDevelopingResourceEnum> resources) {
        this.name = name;
        this.resources = resources;
    }

    /**
     * 根据资源编码获取对应的能力项列表
     *
     * @param resourceSpecCode resourceSpecCode
     * @return
     */
    public static List<String> getNames(String resourceSpecCode) {
        List<String> capacities = new ArrayList<>();
        for (ApplicationDevelopingCapacityEnum value : ApplicationDevelopingCapacityEnum.values()) {
            List<String> resourceNames = value.getResources()
                .stream()
                .map(ApplicationDevelopingResourceEnum::getResourceSpecCode)
                .collect(Collectors.toList());
            if (resourceNames.contains(resourceSpecCode)) {
                capacities.add(value.getName());
            }
        }
        return capacities;
    }

    public String getName() {
        return name;
    }

    public List<ApplicationDevelopingResourceEnum> getResources() {
        return resources;
    }
}
