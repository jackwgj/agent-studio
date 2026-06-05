/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.filter;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import java.util.Locale;

public class CustomMessageInterpolator extends ResourceBundleMessageInterpolator {

    public CustomMessageInterpolator(ResourceBundleLocator resourceBundleLocator){
        super(resourceBundleLocator);
    }
    @Override
    public String interpolate(String messageTemplate, Context context) {
        return super.interpolate(messageTemplate, context, LocalContextHolder.getLocale());
    }
    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return super.interpolate(messageTemplate, context, LocalContextHolder.getLocale());
    }
}
