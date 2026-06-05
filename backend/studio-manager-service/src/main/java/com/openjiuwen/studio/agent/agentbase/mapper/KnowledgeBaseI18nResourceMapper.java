/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseI18nResourceEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库国际化资源 Mapper
 *
 * @author system
 * @since 2025-01-24
 */
@Mapper
public interface KnowledgeBaseI18nResourceMapper {

    /**
     * 查询所有国际化资源
     *
     * @return 所有国际化资源列表
     */
    List<KnowledgeBaseI18nResourceEntity> findAll();

    /**
     * 根据语言查询国际化资源
     *
     * @param locale 语言区域 (zh-cn, en-us)
     * @return 指定语言的国际化资源列表
     */
    List<KnowledgeBaseI18nResourceEntity> findByLocale(@Param("locale") String locale);

    /**
     * 根据国际化Key查询指定语言的资源
     *
     * @param i18nKey 国际化Key
     * @param locale 语言区域
     * @return 翻译文案
     */
    KnowledgeBaseI18nResourceEntity findByKeyAndLocale(@Param("i18nKey") String i18nKey,
        @Param("locale") String locale);

    /**
     * 查询指定语言区域是否存在指定的Key
     *
     * @param i18nKey 国际化Key
     * @param locale 语言区域
     * @return 存在数量
     */
    int countByKeyAndLocale(@Param("i18nKey") String i18nKey, @Param("locale") String locale);

}