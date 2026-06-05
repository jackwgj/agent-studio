/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScheduleVo {
    private String id;

    @Size(max = 100)
    @Pattern(regexp = "^(?!_)(?!\\d)[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20}$")
    private String name;

    @JsonProperty("cronExpr")
    private String cronExpr;

    @Size(max = 10000)
    @Pattern(regexp = "^[0-9a-zA-Z\\u4E00-\\u9FFF\\s!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?\\\\！@#￥%……&*（）——+={}【】、；：‘’“”，。《》？·`~≈≠≤≥±×÷∫∑∏√∞∠∥⊥∪∩∈∉⊆⊇∅∀∃∴∵∝πτℵ∂∇¬∧∨⊕⊗→←↑↓²³]+$")
    @JsonProperty("queryContent")
    private String queryContent;
}
