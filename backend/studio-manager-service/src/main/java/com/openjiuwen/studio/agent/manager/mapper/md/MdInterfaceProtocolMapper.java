/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.manager.entity.md.MdInterfaceProtocol;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MdInterfaceProtocolMapper {
    List<MdInterfaceProtocol> queryAllProtocols();

    void updateVisible(@Param("id") String id, @Param("visible") String visible);
}
