/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.utils;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.DEFAULT_TENANT_TYPE;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.NO_TENANT_INFO;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.common.service.service.TenantServiceUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class TenantTypeUtil {

    public static String getTenantType() {
        String domainId = RequestContextUtils.getRequestUserDomainId();
        if (ObjectUtils.isEmpty(TenantServiceUtil.getTenantInfo(domainId))) {
            return NO_TENANT_INFO;
        }
        String tenantType = TenantServiceUtil.getTenantInfo(domainId).getTenantType();
        if (StringUtils.isEmpty(tenantType)) {
            tenantType = DEFAULT_TENANT_TYPE;
        }
        return tenantType;
    }

}



