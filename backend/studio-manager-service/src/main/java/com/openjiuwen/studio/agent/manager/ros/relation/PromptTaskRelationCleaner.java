/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prompt任务关联资源清理器
 * 负责清理Prompt任务关联的OBS文件+玖问库多表业务数据
 *
 */
@Slf4j
@Service("PromptTask_RelateResourceCleaner")
public class PromptTaskRelationCleaner implements IRelateSourceCleaner {

    // ========== 常量优化：统一归类+语义增强 ==========
    private static final String EVAL_DATA_PATH_TEMPLATE = "EvalData/%s/%s/%s/";

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM `%s` WHERE id IN (%s)";

    private static final List<String> DELETE_TABLE_NAMES = List.of("cases", "history", "job_info", "optimize_info",
        "progress");

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${ros.prompt.schema}")
    private String schema;

    @Value("${ros.jiuwen.schema}")
    private String jiuwenSchema;

    @Autowired
    private PromptObsService obsService;

    @Autowired
    private PromptTaskMapper promptTaskMapper;

    /**
     * 核心清理方法：清理指定ID的Prompt任务关联资源
     *
     * @param ids 待清理的Prompt任务ID集合
     * @param domainId 租户ID
     */
    @Override
    public void clean(List<String> ids, String domainId) {
        // 查询待清理的任务实体
        List<PromptTaskEntity> promptTaskEntities = promptTaskMapper.selectByIds(ids);

        if (CollectionUtils.isEmpty(promptTaskEntities)) {
            return;
        }

        // 流式处理：1.清理OBS文件 2.提取九问任务ID
        List<String> jiuWenIds = promptTaskEntities.stream()
            .peek(this::cleanPromptTaskObsResource)
            .map(PromptTaskEntity::getJiuwenTaskId)
            .filter(StringUtils::isNotBlank)
            .distinct() // 去重：避免同一个九问ID重复执行删除，提升性能
            .collect(Collectors.toList());

        // 核心：批量清理数据库关联表数据
        cleanJiuWenDbRelationData(jiuWenIds);
    }

    /**
     * 清理单个Prompt任务的OBS关联文件
     *
     * @param promptTaskEntity 任务实体
     */
    private void cleanPromptTaskObsResource(PromptTaskEntity promptTaskEntity) {
        String evalDataPath = String.format(EVAL_DATA_PATH_TEMPLATE, promptTaskEntity.getProjectId(),
            promptTaskEntity.getWorkspaceId(), promptTaskEntity.getId());
        obsService.deleteByPrefix(evalDataPath);
        log.info("Prompt task obs info clean successfully, task id: {}", promptTaskEntity.getId());
    }

    /**
     * 批量清理九问库中关联的多表数据
     *
     * @param jiuWenIds 九问任务ID集合
     */
    private void cleanJiuWenDbRelationData(List<String> jiuWenIds) {
        if (CollectionUtils.isEmpty(jiuWenIds)) {
            return;
        }

        // 构建IN条件的占位符 + ID拼接，兼顾性能和防注入
        String jiuWenDbUrl = url.replace(schema, jiuwenSchema);
        DataSourceUserInfo rdsInfo = getRDSInfo();

        // 一个事务中执行所有表的删除操作，保证数据一致性
        Connection conn = null;
        try {
            Class.forName(driverClassName);
            // 获取数据库连接
            conn = DriverManager.getConnection(jiuWenDbUrl, rdsInfo.getUserName(), rdsInfo.getPassWord());
            conn.setAutoCommit(false); // 关闭自动提交，开启事务

            // 遍历所有待删除表，批量执行删除SQL
            for (String tableName : DELETE_TABLE_NAMES) {
                // 1. 动态生成占位符：ids有N个元素，就生成 N个? ，用逗号分隔
                String placeholders = String.join(",", Collections.nCopies(jiuWenIds.size(), "?"));
                // 2. 拼接带占位符的SQL，此时SQL里是 IN (?,?,...) 没有实际ID值
                String deleteSql = String.format(DELETE_SQL_TEMPLATE, tableName, placeholders);
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    // 3. 给占位符逐个赋值，字符串类型用 setString，数字类型用 setLong/setInt
                    for (int i = 0; i < jiuWenIds.size(); i++) {
                        pstmt.setString(i + 1, jiuWenIds.get(i)); // PreparedStatement占位符从1开始
                    }
                    // 4. 执行删除
                    pstmt.executeUpdate();
                    log.info("delete table {} successfully", tableName);
                } catch (SQLException e) {
                    log.error("delete table {} failed", tableName);
                    throw e; // 抛出异常触发事务回滚
                }
            }
            conn.commit(); // 所有删除成功，提交事务
            log.info("delete all table successfully");
        } catch (Exception e) {
            // 异常回滚
            if (conn != null) {
                try {
                    conn.rollback();
                    log.error("delete table failed, rollback successfully");
                } catch (SQLException ex) {
                    log.error("delete table failed, rollback failed");
                }
            }
        } finally {
            // 关闭连接：try-with-resources 语法糖自动关闭，此处兜底
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("conn close failed");
                }
            }
        }
    }

    private DataSourceUserInfo getRDSInfo() {
        return new DataSourceUserInfo(username, password);
    }

    /**
     * RDS连接信息封装类
     */
    @Builder
    @Getter
    @Setter
    private static class DataSourceUserInfo {
        private String userName;

        private String passWord;

        /**
         * 无Builder构造器，适配本地配置快速构建
         */
        public DataSourceUserInfo(String userName, String passWord) {
            this.userName = userName;
            this.passWord = passWord;
        }
    }

}
