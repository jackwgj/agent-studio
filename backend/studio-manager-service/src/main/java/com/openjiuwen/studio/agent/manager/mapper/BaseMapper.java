/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import java.util.List;
import java.util.Map;

/**
 * BaseMapper
 *
 */
public interface BaseMapper<T, P> {
    /**
     * 保存记录
     *
     * @param obj 待保存的DAO
     * @return 行数
     */
    int save(T obj);

    /**
     * 批量保存记录
     *
     * @param listObj 待保存的DAO列表
     * @return 行数
     */
    int saveBatch(List<T> listObj);

    /**
     * 更新记录
     *
     * @param obj 待更新的记录
     * @return 影响的行数
     */
    int update(T obj);

    /**
     * 批量更新记录
     *
     * @param listObj 待批量更新的记录
     * @return 影响的行数
     */
    int updateBatch(List<T> listObj);

    /**
     * 根据主键删除记录
     *
     * @param id 主键ID
     * @return 影响的行数
     */
    int deleteByPK(P id);

    /**
     * 根据条件删除部分
     *
     * @param cons 删除的条件
     * @return 影响的行数
     */
    int deleteByCons(Map<String, Object> cons);

    /**
     * 统计记录数
     *
     * @return 记录数
     */
    long count();

    /**
     * 统计满足指定条件的记录数
     *
     * @param cons 查询条件
     * @return 满足条件的记录行数
     */
    int count(Map<String, Object> cons);

    /**
     * 通过主键查找记录
     *
     * @param id 主键ID
     * @return 记录
     */
    T findByPK(P id);

    /**
     * 通过指定的条件查找记录
     *
     * @param cons 指定的条件
     * @return 记录列表
     */
    List<T> findByCons(Map<String, Object> cons);

    /**
     * 判断指定的记录是否存在
     *
     * @param id 主键Id
     * @return
     */
    int checkExist(P id);

    /**
     * 判断满足条件的记录是否存在
     *
     * @param conditions
     * @return
     */
    long checkExist(Map<String, Object> conditions);
}
