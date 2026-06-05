package com.openjiuwen.studio.agent.space.api.vo.response.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KooPageEnvConfigInfo {
    private String editorUrl;

    private String aiUrl;

    private String apiUrl;

    private String collaborationUrl;

    private String multiTableCollaborationUrl;

    private String flowDiagramUrl;

    private String monacoEditorUrl;

    private String dolphinWebUrl;

    private String obsPrefix;

    private String DataAccessURL;

    private String appId;
}
