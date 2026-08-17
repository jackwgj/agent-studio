package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.entity.SkillEntity;
import com.openjiuwen.studio.agent.manager.mapper.SkillMapper;
import com.openjiuwen.studio.conversation.application.dto.ConversationSkillContext;
import com.openjiuwen.studio.conversation.application.dto.ConversationSkillVo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationSkillResolverTest {

    private final SkillMapper skillMapper = mock(SkillMapper.class);
    private final ConversationSkillResolver resolver = new ConversationSkillResolver(skillMapper);

    @Test
    void listAvailable_只返回当前边界内可执行目录项() {
        when(skillMapper.search(any(), eq(0), eq(1000), isNull(), isNull(), eq(0)))
            .thenReturn(List.of(
                skill("s1", "d1", "p1", "w1", "developed", "v1", "u1/skills/s1/v1/a.zip"),
                skill("s2", "d1", "p1", "w1", "developing", "v2", "u1/skills/s2/v2/b.zip"),
                skill("s3", "d1", "p1", "w1", "developed", "v3", ""),
                skill("s4", "d2", "p1", "w1", "developed", "v4", "u1/skills/s4/v4/a.zip")));

        List<ConversationSkillVo> result = resolver.listAvailable("p1", "w1", "d1");

        assertEquals(List.of("s1"), result.stream().map(ConversationSkillVo::getSkillId).toList());
        org.mockito.ArgumentCaptor<SkillEntity> condition = org.mockito.ArgumentCaptor.forClass(SkillEntity.class);
        verify(skillMapper, times(1)).search(condition.capture(), eq(0), eq(1000), isNull(), isNull(), eq(0));
        assertEquals("p1", condition.getValue().getProjectId());
        assertEquals("w1", condition.getValue().getWorkspaceId());
        assertEquals("d1", condition.getValue().getDomainId());
        assertEquals("developed", condition.getValue().getStatus());
    }

    @Test
    void listAvailable_达到页大小时继续读取下一页() {
        List<SkillDetails> firstPage = IntStream.range(0, 1000)
            .mapToObj(i -> skill("s" + i, "d1", "p1", "w1", "developed", "v" + i,
                "u1/skills/s" + i + "/v" + i + "/a.zip"))
            .toList();
        when(skillMapper.search(any(), eq(0), eq(1000), isNull(), isNull(), eq(0))).thenReturn(firstPage);
        when(skillMapper.search(any(), eq(1000), eq(1000), isNull(), isNull(), eq(0))).thenReturn(List.of());

        assertEquals(1000, resolver.listAvailable("p1", "w1", "d1").size());
        verify(skillMapper).search(any(), eq(1000), eq(1000), isNull(), isNull(), eq(0));
    }

    @Test
    void resolveForRun_仅推荐当前边界内的请求技能() {
        when(skillMapper.search(any(), eq(0), eq(1000), isNull(), isNull(), eq(0)))
            .thenReturn(List.of(
                skill("s1", "d1", "p1", "w1", "developed", "v1", "u1/skills/s1/v1/a.zip"),
                skill("s2", "d1", "p1", "w1", "developing", "v2", "u1/skills/s2/v2/b.zip")));

        ConversationSkillContext result = resolver.resolveForRun("p1", "w1", "d1", List.of("s2", "s1", "s3"));

        assertEquals(List.of("s1"), result.getCatalog().stream().map(item -> item.getSkillId()).toList());
        assertEquals(List.of("s1"), result.getRecommendedSkillIds());
        assertEquals("v1", result.getCatalog().get(0).getVersionId());
        assertEquals("u1/skills/s1/v1/a.zip", result.getCatalog().get(0).getObjectKey());
    }

    private SkillDetails skill(String id, String domainId, String projectId, String workspaceId,
                               String status, String versionId, String objectKey) {
        return new SkillDetails().setSkillId(id).setDomainId(domainId).setProjectId(projectId)
            .setWorkspaceId(workspaceId).setStatus(status).setLatestVersion(versionId)
            .setName("skill-" + id).setDescription("description-" + id).setObsPath(objectKey);
    }
}
