/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.manager.entity.md.UserModelSubscribeSettingPo;

import lombok.RequiredArgsConstructor;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * UserModelSubscribeSettingsDao
 *
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserModelSubscribeSettingsDao {
    private final com.openjiuwen.studio.agent.manager.mapper.md.UserModelSubscribeSettingsMapper
        userModelSubscribeSettingsMapper;

    private static final String DOMAIN_ID_FILTER_KEY = "DOMAIN_ID";
    private static final String IS_AUTO_SUBSCRIBE_NEW_MODEL = "IS_AUTO_SUBSCRIBE_NEW_MODEL";

    /**
     * queryUserSubscribeSetting
     *
     * @param domainId domainId
     * @return UserModelSubscribeSetting
     */
    public UserModelSubscribeSettingPo queryUserSubscribeSetting(String domainId) {
        if (StringUtils.isBlank(domainId)) {
            return null;
        }

        List<UserModelSubscribeSettingPo> userModelSubscribeSettingPos = userModelSubscribeSettingsMapper.findByCons(
            Collections.singletonMap(DOMAIN_ID_FILTER_KEY, domainId));

        return CollectionUtils.isEmpty(userModelSubscribeSettingPos) ? null : userModelSubscribeSettingPos.get(0);
    }

    /**
     * saveUserSubscribeSetting
     *
     * @param userModelSubscribeSettingPo userModelSubscribeSettingPo
     * @return int
     */
    public int saveUserSubscribeSetting(UserModelSubscribeSettingPo userModelSubscribeSettingPo) {
        return userModelSubscribeSettingsMapper.save(userModelSubscribeSettingPo);
    }

    /**
     * updateUserSubscribeSetting
     *
     * @param userModelSubscribeSettingPo userModelSubscribeSettingPo
     * @return int
     */
    public int updateUserSubscribeSetting(UserModelSubscribeSettingPo userModelSubscribeSettingPo) {
        return userModelSubscribeSettingsMapper.update(userModelSubscribeSettingPo);
    }

    public List<UserModelSubscribeSettingPo> queryUserSettingByAutoSubscribe() {
        return userModelSubscribeSettingsMapper.findByCons(Collections.singletonMap(IS_AUTO_SUBSCRIBE_NEW_MODEL, 1));
    }
}
