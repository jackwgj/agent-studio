/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.config;

import com.openjiuwen.studio.agent.space.app.constant.Constant;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 知识数据集相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "unidata.knowledge.dataset")
public class AgentBuilderConfiguration {
    /******** 不同数据集类型总文件差异限制 start ********/
    /**
     * 文档类型总大小限制，默认500M
     */
    private long textAllFileSizeLimit = 500 * Constant.MB;

    /**
     * 上传文件总数
     */
    private int allFileCount = 5;

    /**
     * 图片-类型类型总大小限制，默认1G
     */
    private long imageAllFileSizeLimit = Constant.GB;
    /******** 不同数据集类型总文件差异限制 end ********/

    /******** 单个文件公共限制 start ********/
    /**
     * 文件名长度限制，默认80
     * <br>
     * Linux文件名长度限制255字节，对应85个中文字符
     */
    private int fileNameLengthLimit = 80;

    /**
     * 绝对路径长度限制，保护文件名和文件后缀，默认200
     * <br>
     * 系统处理时要将文件存入内部OBS桶，OBS绝对路径长度限制1023字节，对应341个中文字符，预取系统预留的根目录长度（141个中文字符）
     */
    private int absolutePathLengthLimit = 200;
    /******** 单个文件公共限制 end ********/

    /******** 不同数据集类型单个文件差异限制 start ********/
    /**
     * 文本文件单个最大大小，默认5M
     */
    private long textFileSizeLimit = 5 * Constant.MB;

    /**
     * 图片文件单个最大大小，默认10M
     */
    private long imageFileSizeLimit = 10 * Constant.MB;
    /******** 不同数据集类型单个文件差异限制 end ********/
}
