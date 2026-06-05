package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Date;

/**
 * Agent 实体类，对应数据库表 t_agent
 */
@Data
@TableName("t_agent")
public class Agent {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * agent 唯一标识
     */
    @TableField("agent_id")
    private String agentId;


    /**
     * agent 类型
     */
    @TableField("type")
    private String type;

    /**
     * 项目唯一标识
     */
    @TableField("domain_id")
    private String domainId;

    /**
     * agent 状态 active上线，inactive下线
     */
    @TableField("status")
    private String status;

    /**
     * 来源
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("created_date")
    private Date createdDate;

    /**
     * 更新时间
     */
    @TableField("last_updated_date")
    private Date lastUpdatedDate;

    /**
     * 是否已删除（0-未删除，1-已删除）
     */
    private Boolean deleted = false;
}