/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.enums;

import com.openjiuwen.studio.agent.runtime.entity.NoMatchContentEntity;

import java.util.Arrays;

public class F1AgentNoMatchContent {

    public static final NoMatchContentEntity AGENT_CONTENT = NoMatchContentEntity.builder()
            .prompt("您好！很高兴您想创建一个应用。请问这个应用的主要用途是什么？是用于解决什么问题或者提供什么服务呢？我们为您准备了优质的用于生成应用的prompt模板，可以为您提供一些参考")
            .template(Arrays.asList("创建一个用于作文点评的应用，需要对学生作文中的语句优化和润色，让作文整体更加饱满。",
                    "创建一个短视频带货文案生成的应用，需根据给出的商品信息撰写一段直播带货口播文案，放大商品的亮点价值，激发购买欲。",
                    "帮我生成一个工作汇报助手，能够根据我提供的资料生成项目总结汇报、周报或会议纪要。")).build();

    public static final NoMatchContentEntity PLUGIN_CONTENT = NoMatchContentEntity.builder()
            .prompt("好的！我将为您创建插件。请你描述一下应用具备的功能和用途，比如你可以这样对我说：帮我创建一个能够查天气的插件！我们为您准备了优质的用于生成插件的prompt模板，可以为您提供一些参考")
            .template(Arrays.asList("帮我创建一个查天气的插件，需要支持查询国内的城市，并根据城市查询当地的天气、温度和空气质量信息。",
                    "帮我创建一个汇率查询的插件，支持实时货币汇率查询换算。",
                    "帮我创建一个快递查询的插件，支持通过快递单号和快递公司，查询国内最新的物流信息。")).build();

    public static final NoMatchContentEntity WORKFLOW_CONTENT = NoMatchContentEntity.builder()
            .prompt("我会很乐意帮助你创建一条工作流。首先，我需要知道你的工作流将被用来做什么？这将有助于我为它取一个合适的名字并提供一个描述。我们为您准备了优质的用于生成工作流的prompt模板，可以为您提供一些参考")
            .template(Arrays.asList("我想要生成一条用于文章批量改写的工作流，需要指出文章中的错别字，优化句式、段落信息。",
                    "我想要生成一条用于信息总结的工作流，根据输入信息总结成100字以内的输出内容。",
                    "我想要生成一条用于省份名称提取的工作流，根据输入内容提取文章中出现的省份。")).build();

    public static NoMatchContentEntity enumOf(String type) {
        switch (type) {
            case "应用":
                return AGENT_CONTENT;
            case "助手":
                return AGENT_CONTENT;
            case "插件":
                return PLUGIN_CONTENT;
            case "工作流":
                return WORKFLOW_CONTENT;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
