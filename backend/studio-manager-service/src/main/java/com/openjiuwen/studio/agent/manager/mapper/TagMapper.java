/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.Tag;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * tag 数据库操作mapper
 *
 */
@Mapper
public interface TagMapper {
    /**
     * 根据project id从数据库获取tag列表
     *
     * @return 标签列表
     */
    List<Tag> select();
}
