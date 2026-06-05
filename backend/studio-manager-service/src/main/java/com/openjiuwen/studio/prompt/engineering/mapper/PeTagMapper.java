/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.entity.PeTag;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 针对表【t_pe_tag(prompt标签表)】的数据库操作Mapper
 *
 */
@Mapper
@Component("PeTagMapper")
public interface PeTagMapper {
    /**
     * 查询标签信息
     *
     * @return 返回所有标签信息
     */
    List<PeTag> queryAll();

    /**
     * 创建标签
     *
     * @param peTag 标签信息
     */
    void insertOneTag(PeTag peTag);

    /**
     * 根据ids查询标签信息
     *
     * @param ids 标签id集合
     * @return 标签信息
     */
    List<PeTag> queryByIds(List<String> ids, String workspaceId);

    /**
     * 使用标签id删除单个标签
     *
     * @param id 标签id
     */
    void deleteById(String id, String workspaceId);

    /**
     * 判断标签名称是否存在
     *
     * @param name 标签名称
     * @return 存在返回true， 否则不返回
     */
    Boolean isExist(String name, String workspaceId);

    /**
     * 更新标签
     *
     * @param peTag 标签信息
     */
    void updateOneTag(PeTag peTag);
}
