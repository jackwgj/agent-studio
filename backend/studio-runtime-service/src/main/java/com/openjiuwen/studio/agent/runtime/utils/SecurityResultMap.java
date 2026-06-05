/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * SecurityResultMap
 *
 */
@Slf4j
public class SecurityResultMap {
    private static final Map<String, String> securityMap = new HashMap<>();

    private static final String HC_ACCEPT = "ACCEPT";

    private static final String HC_VALIDATE = "VALIDATE";

    private static final String HC_REJECT = "REJECT";

    private static final String HCS_OR_HCSO_ACCEPT = "pass";

    private static final String HCS_OR_HCSO_VALIDATE = "review";

    private static final String HCS_OR_HCSO_REJECT = "block";

    static {
        securityMap.put(HCS_OR_HCSO_ACCEPT, HC_ACCEPT);
        securityMap.put(HCS_OR_HCSO_VALIDATE, HC_VALIDATE);
        securityMap.put(HCS_OR_HCSO_REJECT, HC_REJECT);
    }

    public static String getSecurityMap(String oriResult) {
        log.info("original result is : {}", oriResult);
        return securityMap.get(oriResult);
    }
}
