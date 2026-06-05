package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

/**
 * Created by Fang Zhen on 2024/4/7.
 */
@Getter
public enum Judgment {
    YES("Y"),
    NO("N");

    private final String value;

    Judgment(String value) {
        this.value = value;
    }
}
