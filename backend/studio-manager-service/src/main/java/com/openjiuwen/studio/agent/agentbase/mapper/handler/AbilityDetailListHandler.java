/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.mapper.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.agentbase.model.AbilityDetail;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AbilityDetailListHandler extends BaseTypeHandler<List<AbilityDetail>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<AbilityDetail> parameter, JdbcType jdbcType)
        throws SQLException {
        try {
            // 写入数据库时，正常转回 JSON 字符串
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            log.error("Fail to transfer AbilityDetail List to JSON", e);
            ps.setString(i, "[]");
        }
    }

    @Override
    public List<AbilityDetail> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<AbilityDetail> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<AbilityDetail> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<AbilityDetail> parse(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 读成通用的 JsonNode
            JsonNode root = objectMapper.readTree(content);
            List<AbilityDetail> list = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode itemNode : root) {
                    AbilityDetail abilityDetail = new AbilityDetail();

                    // 映射基本字段
                    abilityDetail.setAbilityCode(itemNode.path("ability_code").asText());
                    abilityDetail.setEnable(itemNode.path("enable").asBoolean());

                    // 把 sub_ability 整体转为 String
                    JsonNode subNode = itemNode.path("sub_ability");
                    if (!subNode.isMissingNode() && !subNode.isNull()) {
                        abilityDetail.setSubAbility(subNode.toString());
                    }

                    list.add(abilityDetail);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Fail to transfer used_abilities to AbilityDetail list", e);
            return new ArrayList<>();
        }
    }
}