package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Latency {

    /**
     * 总耗时，单位 秒
     */
    private double overall;
}
