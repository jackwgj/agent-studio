package com.openjiuwen.studio.agent.space.common.model.sysparam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysParam {

    private String id;

    private String name;

    private String value;

    /**
     * 数据字典类型：
     *  0：文本（TEXT）
     *  1：选择（SELECT）
     *  2：单选（RADIO）
     *  3：多选（CHECK）
     *  4：密码（PASSWORD）
     */
    private String type;

    private String editType;

    private String moduleId;

    /**
     * 是否用户前台展示
     */
    private String pageShow;

    private Timestamp createdDate;

    private String createDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdateDate;

    private String lastUpdatedByUserId;

    private String tenantId;

    public Timestamp getCreatedDate() {
        return createdDate == null ? null : (Timestamp) createdDate.clone();
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate == null ? null : (Timestamp) createdDate.clone();
    }

    public Timestamp getLastUpdatedDate() {
        return lastUpdatedDate == null ? null : (Timestamp) lastUpdatedDate.clone();
    }

    public void setLastUpdatedDate(Timestamp lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate == null ? null : (Timestamp) lastUpdatedDate.clone();
    }
}
