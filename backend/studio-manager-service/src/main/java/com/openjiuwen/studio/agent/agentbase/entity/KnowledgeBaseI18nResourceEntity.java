/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import java.io.Serializable;

/**
 * 知识库国际化资源实体
 *
 * @author system
 * @since 2025-01-24
 */
public class KnowledgeBaseI18nResourceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 国际化Key (业务唯一标识)
     */
    private String i18nKey;

    /**
     * 语言区域 (zh-cn, en-us)
     */
    private String locale;

    /**
     * 翻译后的展示文案
     */
    private String message;

    public Long getId() {
        return id;
    }

    public KnowledgeBaseI18nResourceEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public KnowledgeBaseI18nResourceEntity setI18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public KnowledgeBaseI18nResourceEntity setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public KnowledgeBaseI18nResourceEntity setMessage(String message) {
        this.message = message;
        return this;
    }
}