/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.filter;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidatorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new LocaleInterceptor())
            .addPathPatterns("/**");
    }

    @Bean
    public Validator validator() {
        PlatformResourceBundleLocator resourceBundleLocator =
            new PlatformResourceBundleLocator("ValidationMessages");

        com.openjiuwen.studio.agent.manager.filter.CustomMessageInterpolator messageInterpolator =
            new CustomMessageInterpolator(resourceBundleLocator);


        ValidatorFactory factory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(messageInterpolator)
            .buildValidatorFactory();

        return  factory.getValidator();
    }
}
