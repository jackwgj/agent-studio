/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.dao;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户PO
 *
 */
@Data
public class UserEntity {

    @NotNull
    private String id;

    @NotEmpty
    private String name;

    private String source;

    private String password;

    private String salt;
}
