/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.service;

/**
 * 这是一个“钩子”接口。
 * Common模块不知道具体的SCC怎么写，所以定义这个接口，让上层模块把SCC逻辑传进来。
 */
public interface LegacyCryptoHandler {
    String encrypt(String text);
    String decrypt(String text);
}
