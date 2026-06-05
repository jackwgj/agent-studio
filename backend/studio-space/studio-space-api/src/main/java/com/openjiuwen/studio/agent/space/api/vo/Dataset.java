package com.openjiuwen.studio.agent.space.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Fang Zhen on 2024/5/15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset { // 实际是知识库
    private String id;

    private String name;

    private String description;

    private String status;

    private String scope;
}
