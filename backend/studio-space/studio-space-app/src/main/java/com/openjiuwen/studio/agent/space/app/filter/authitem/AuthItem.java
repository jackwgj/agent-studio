/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.authitem;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 鉴权接口基类
 */
public interface AuthItem {
    boolean isMatch(HttpServletRequest request, String urlPath);

    boolean auth(HttpServletRequest request, HttpServletResponse response);
}
