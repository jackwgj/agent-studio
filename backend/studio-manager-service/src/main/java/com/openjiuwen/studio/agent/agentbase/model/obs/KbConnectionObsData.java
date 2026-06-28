/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.obs;

import com.openjiuwen.studio.agent.agentbase.model.AbilityDetail;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库连接文件（OBS）的数据结构。
 * <p>
 * 写入 kb-connection/ir/connection/{connectionId}.json，供 Python 运行时读取。
 * 字段名通过 @JsonProperty 锁定为驼峰，与历史 Map 序列化结果保持一致（JacksonUtils 全局
 * SNAKE_CASE 命名策略不作用于 Map key，故旧实现产出的就是驼峰 key）。
 * </p>
 *
 * @since 2026-06-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbConnectionObsData {

    @JsonProperty("connectionId")
    private String connectionId;

    @JsonProperty("connectorId")
    private String connectorId;

    @JsonProperty("connectorType")
    private String connectorType;

    @JsonProperty("connectorName")
    private String connectorName;

    /**
     * 知识源模式标记：Python 端读到 CUSTOM 时走 CUSTOM 检索语义。
     */
    @JsonProperty("knowledgeSource")
    private String knowledgeSource;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("krb5File")
    private String krb5File;

    @JsonProperty("keytabFile")
    private String keytabFile;

    @JsonProperty("usedAbilities")
    private List<AbilityDetail> usedAbilities;

    /**
     * 连接参数列表。SECRET 类型字段在 manager 端已加密，写入 OBS 时直接使用。
     */
    @JsonProperty("params")
    private List<KbConnectionParamData> params;

    /**
     * 单条连接参数（code/value），独立定义以避免序列化时受全局 SNAKE_CASE 影响。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KbConnectionParamData {

        @JsonProperty("code")
        private String code;

        @JsonProperty("value")
        private String value;
    }
}
