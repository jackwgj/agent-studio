package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_agent_default")
public class AgentDefault {

    @TableId("id")
    private String id;

    /**
     * agent 唯一标识
     */
    @TableField("agent_id")
    private String agentId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;

    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("created_date")
    private LocalDateTime createdDate;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("type")
    private Integer type;

    @TableField("sort")
    private Integer sort;
}