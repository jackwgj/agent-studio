 /*
  * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
  */
 package com.openjiuwen.studio.agent.manager.mapper;

 import com.openjiuwen.studio.agent.manager.entity.HistoryAgentEntity;
 import org.apache.ibatis.annotations.Mapper;
 import org.apache.ibatis.annotations.Param;

 import java.util.Date;
 import java.util.List;

 @Mapper
 public interface HistoryAgentMapper {

     List<HistoryAgentEntity> findByUpdatedOnLessThan(@Param("time") Date time);

     int insert(HistoryAgentEntity entity);

     int deleteByHistoryId(@Param("historyId") String historyId);
 }

