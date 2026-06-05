package com.openjiuwen.studio.agent.space.api.vo.response.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontConfigInfo {
    private String agentStudioUrl;

    private KooPageEnvConfigInfo kooPageEnvConfigInfo;
}
