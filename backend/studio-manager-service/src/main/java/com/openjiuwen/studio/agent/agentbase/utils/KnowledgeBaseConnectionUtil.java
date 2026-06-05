/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import com.google.common.collect.Maps;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;

import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Map;

/**
 * 第三方知识库连接相关的工具类
 *
 * @since 2025-08-29
 */
public class KnowledgeBaseConnectionUtil {

    /**
     * 检查两个三方知识库连接信息是否完全相同
     *
     * @param param1
     * @param param2
     * @return
     */
    public static boolean isSameConnection(List<KnowledgeBaseConnectionParam> param1,
        List<KnowledgeBaseConnectionParam> param2) {
        if (param1.size() != param2.size()) {
            return false;
        }
        Map<String, String> map1 = Maps.newTreeMap();
        param1.forEach(item -> {
            map1.put(item.getCode(), item.getValue());
        });
        Map<String, String> map2 = Maps.newTreeMap();
        param2.forEach(item -> {
            map2.put(item.getCode(), item.getValue());
        });
        StringBuilder strToCompare1 = new StringBuilder();
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            strToCompare1.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        StringBuilder strToCompare2 = new StringBuilder();
        for (Map.Entry<String, String> entry : map2.entrySet()) {
            strToCompare2.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        return Strings.CS.equals(strToCompare1, strToCompare2);
    }

}
