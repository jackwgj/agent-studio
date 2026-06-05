/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.constant;

/**
 * 公共常量
 */
public interface CommonConstant {

    /**
     * 通用常量：无限
     */
    String INFINITY = "-1";

     String DEFAULT_TENANT_TYPE = "default_tenant_type";

     String DEFAULT_DOMAIN_ID = "default_domain_id";

    String NO_TENANT_INFO = "no_tenant_info";

    /**
     * 租户类型
     */
    interface TenantType {

        String TENANT_TYPE_2B = "B";

        String TENANT_TYPE_2C = "C";
    }

    /**
     * 租户状态
     */
    interface TenantStatus {

        String ACTIVE = "active";

        String INACTIVE = "inactive";

        String DELETED = "deleted";

        String FREEZE = "freeze";
    }

    /**
     * 资源状态
     */
    interface ResourceStatus {

        String ACTIVE = "ACTIVE";

        // 资源订购成功
        String SUBSCRIBED = "SUBSCRIBED";

        String INACTIVE = "INACTIVE";

        String DELETED = "DELETED";

        String CLEANED = "CLEANED";

    }

    /**
     * 通用操作
     */
    interface CommonOperType {

        String ADD = "add";

        String DELETE = "delete";

    }

    /**
     * SKU属性类型
     */
    interface AttrType {
        /**
         * 属性类型：计数型
         */
        String BOOLEAN = "1";

        /**
         * 属性类型：计数型
         */
        String COUNT = "2";
    }
}
