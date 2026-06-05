/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.config;

import com.openjiuwen.studio.agent.common.crypt.Cipher;
import com.openjiuwen.studio.agent.common.crypt.Ciphers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CryptConfiguration {
    @Value("${system.crypt.name:}")
    private String defaultCryptAlgName;

    @Bean
    public Ciphers ciphers(List<Cipher> ciphers) {
        return new Ciphers(ciphers, defaultCryptAlgName);
    }
}