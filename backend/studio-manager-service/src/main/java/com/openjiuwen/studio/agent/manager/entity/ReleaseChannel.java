/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.VersionChannelInfo;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Date;

/**
 * 功能描述 发布通道数据库实体
 *
 */
@Data
@NoArgsConstructor
public class ReleaseChannel {
    /**
     * 发布通道ID
     */
    private String id;

    /**
     * 租户唯一标识
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 空间唯一标识
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 应用类型
     */
    @JsonProperty("app_type")
    private String appType;

    /**
     * 发布版本唯一标识
     */
    @JsonProperty("version_id")
    private String versionId;

    /**
     * 发布版本号
     */
    @JsonProperty("version_name")
    private String versionName;

    /**
     * 通道类型
     */
    @JsonProperty("channel_type")
    private String channelType;

    /**
     * 访问入口
     */
    @JsonProperty("entry_point")
    private String entryPoint;

    /**
     * 访问二维码
     */
    @JsonProperty("qr_code")
    private String qrCode;

    /**
     * 访问的短ID
     */
    @JsonProperty("short_code")
    private String shortCode;

    /**
     * 发布通道元数据
     */
    private String metadata;

    /**
     * 发布通道状态
     */
    private String status;

    /**
     * 发布通道结果详情
     */
    private String detail;

    /**
     * 版本创建者
     */
    private String creator;

    /**
     * 版本创建者user id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 版本发布时间
     */
    @JsonProperty("released_on")
    private Date releasedOn;

    /**
     * 访问地址
     */
    private String url;

    @JsonProperty("visibility_scope")
    private String visibilityScope;

    /**
     * 发布通道子类型（如：deepresearch）
     */
    @JsonProperty("sub_type")
    private String subType;

    @JsonProperty("call_count")
    private Integer callCount;

    /**
     * 转换DTO Info
     */
    public VersionChannelInfo convertToInfo() {
        VersionChannelInfo channelInfo = new VersionChannelInfo();
        channelInfo.setId(getId());
        channelInfo.setAppId(getAppId());
        channelInfo.setAppType(getAppType());
        channelInfo.setVersionId(getVersionId());
        channelInfo.setVersionName(getVersionName());
        channelInfo.setChannelType(getChannelType());
        channelInfo.setEntryPoint(getEntryPoint());
        channelInfo.setQrCode(getQrCode());
        channelInfo.setShortCode(getShortCode());
        channelInfo.setMetadata(parseChannelMetadata());
        channelInfo.setStatus(getStatus());
        channelInfo.setReleaseTime(getReleasedOn());
        channelInfo.setSubType(getSubType());
        channelInfo.setUrl(getUrl());
        channelInfo.setVisibilityScope(getVisibilityScope());
        channelInfo.setCallCount(getCallCount());
        return channelInfo;
    }

    private Object parseChannelMetadata() {
        if (getMetadata() != null) {
            if (getChannelType().equals(CommonConstant.APP_STORE_CHANNEL)) {
                // 发布通道为APP_STORE时，metadata转换为tag list
                String[] strArray = JsonUtils.json2ObjQuietly(getMetadata(), String[].class);
                if (strArray != null) {
                    return Arrays.stream(strArray).toList();
                }
            }
        }
        return null;
    }
}
