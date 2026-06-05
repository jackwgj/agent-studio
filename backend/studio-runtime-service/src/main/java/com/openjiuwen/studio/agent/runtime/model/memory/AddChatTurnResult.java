/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import lombok.Data;

import java.util.List;

/**
 * AddChatTurnResp
 *
 */
@Data
public class AddChatTurnResult {
    private Boolean isAdded;

    private Boolean isExtracted;

    private Boolean isUerProfileRefreshed;

    private List<UserProfileTag> tags;

    /**
     * 返回会话添加失败result
     *
     * @return AddChatTurnResult
     */
    public static AddChatTurnResult failResult() {
        AddChatTurnResult failedResult = new AddChatTurnResult();

        failedResult.setIsAdded(false);
        failedResult.setIsExtracted(false);
        failedResult.setIsUerProfileRefreshed(false);

        return failedResult;
    }

    /**
     * 返回只添加缓存区, 未触发记忆抽取的结果
     *
     * @return AddChatTurnResult
     */
    public static AddChatTurnResult onlyAddResult() {
        AddChatTurnResult onlyAddResult = new AddChatTurnResult();

        onlyAddResult.setIsAdded(true);
        onlyAddResult.setIsExtracted(false);
        onlyAddResult.setIsUerProfileRefreshed(false);

        return onlyAddResult;
    }

    /**
     * 返回记忆抽取的结果
     *
     * @param isUserProfileRefresh isUserProfileRefresh
     * @param tags tags
     * @return AddChatTurnResult
     */
    public static AddChatTurnResult extractResult(boolean isUserProfileRefresh, List<UserProfileTag> tags) {
        AddChatTurnResult extractResult = new AddChatTurnResult();

        extractResult.setIsAdded(true);
        extractResult.setIsExtracted(true);

        extractResult.setIsUerProfileRefreshed(isUserProfileRefresh);
        extractResult.setTags(tags);

        return extractResult;
    }
}
