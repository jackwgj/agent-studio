/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.utils.SqlLikeEscapeHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SqlLikeEscapeHelperTest {

    @Test
    public void escapeLikeCharacters_input_null() {
        assertNull(SqlLikeEscapeHelper.escapeLikeCharacters(null));
    }

    @Test
    public void escapeLikeCharacters_input_special_character() {
        assertEquals("///%/'/_/\\/\\/\"", SqlLikeEscapeHelper.escapeLikeCharacters("/%'_\\\\\""));
    }
}
