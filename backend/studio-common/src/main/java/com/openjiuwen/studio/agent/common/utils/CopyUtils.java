/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CopyUtils {

    public static Object copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static Object copyProperties(Object source, Object target, boolean isNeedCommonAttrsTrans) {
        BeanUtils.copyProperties(source, target);
        if (isNeedCommonAttrsTrans) {
            try {
                Class<?> cls = target.getClass();

                Method getCreatedDate = cls.getMethod("getCreatedDate");
                Method setCreateDate = cls.getMethod("setCreateDate", String.class);
                setCreateDate.invoke(target, DateUtils.formatDate((Timestamp) getCreatedDate.invoke(target)));

                Method getLastUpdatedDate = cls.getMethod("getLastUpdatedDate");
                Method setLastUpdateDate = cls.getMethod("setLastUpdateDate", String.class);
                setLastUpdateDate.invoke(target, DateUtils.formatDate((Timestamp) getLastUpdatedDate.invoke(target)));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new AgentStudioException(e.getMessage());
            }
        }
        return target;
    }

    /**
     * 拷贝数组
     *
     * @param sourceList  源数组
     * @param targetClass 目标数组类型
     * @param <T>         源数组类
     * @param <R>         目标数组类
     * @return 目标数组
     */
    public static <T, R> List<R> copyList(List<T> sourceList, Class<R> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<R> targetList = new ArrayList<>();
        sourceList.forEach(source -> {
            try {
                @SuppressWarnings("unchecked")
                R target = (R) copyProperties(source, targetClass.getDeclaredConstructor().newInstance());
                targetList.add(target);
            } catch (Exception e) {
                log.error("Copy list error: {}", e.getMessage());
            }
        });
        return targetList;
    }

    /**
     * 拷贝数组
     *
     * @param sourceList             源数组
     * @param targetClass            目标数组类型
     * @param <T>                    源数组类
     * @param <R>                    目标数组类
     * @param isNeedCommonAttrsTrans 是否需要对公共属性做逻辑转换
     * @return 目标数组
     */
    public static <T, R> List<R> copyList(List<T> sourceList, Class<R> targetClass, boolean isNeedCommonAttrsTrans) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<R> targetList = new ArrayList<>();
        sourceList.forEach(source -> {
            try {
                @SuppressWarnings("unchecked")
                R target = (R) copyProperties(source, targetClass.getDeclaredConstructor().newInstance(), isNeedCommonAttrsTrans);
                targetList.add(target);
            } catch (Exception e) {
                log.error("Copy list error: {}", e.getMessage());
            }
        });
        return targetList;
    }
}
