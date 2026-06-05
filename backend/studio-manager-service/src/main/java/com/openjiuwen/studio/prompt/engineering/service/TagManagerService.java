/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.annotation.LimitOffset;
import com.openjiuwen.studio.prompt.engineering.dto.ListTagsQo;
import com.openjiuwen.studio.prompt.engineering.dto.TagDto;
import com.openjiuwen.studio.prompt.engineering.dto.TagListVo;
import com.openjiuwen.studio.prompt.engineering.dto.TagVo;
import com.openjiuwen.studio.prompt.engineering.entity.PeTag;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTagMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

/**
 * TagManager service
 *
 */
@Slf4j
@Component
public class TagManagerService implements ITagManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagManagerService.class);

    @Autowired
    @Qualifier("PeTagMapper")
    private PeTagMapper peTagMapper;

    @Resource
    private PeTaskMapper peTaskMapper;

    /**
     *
     */
    public static final Integer TAG_TASK_MAPPING = 0;

    /**
     * 创建标签
     *
     * @param body body
     * @return 创建成功后的实体
     */

    public TagVo createTag(String projectId, String workspaceId, TagDto body) {
        log.info("operation log {} : create tag", projectId);
        String id = UUID.randomUUID().toString();
        if (Boolean.TRUE.equals(peTagMapper.isExist(body.getName(), workspaceId))) {
            throw new AgentStudioException(StudioError.TAG_NAME_IS_EXIST);
        }
        try {
            peTagMapper.insertOneTag(PeTag.builder().id(id).name(body.getName()).workspaceId(workspaceId).build());
            List<PeTag> peTags = peTagMapper.queryByIds(Collections.singletonList(id), workspaceId);
            TagVo tagVo = new TagVo();
            BeanUtils.copyProperties(peTags.get(0), tagVo);
            return tagVo;
        } catch (Exception e) {
            LOGGER.error("Failed to insert tag {} into database", body.getName(), e);
            throw new AgentStudioException(StudioError.FAILED_TO_CREATE_TAG);
        }
    }

    /**
     * 根据标签ID删除标签信息，若标签未绑定任务，则可以直接删除，否则返回当前绑定的任务数
     *
     * @param tagId tagId
     * @return 绑定的任务数量
     */

    public Integer deleteTag(String projectId, String tagId, String workspaceId) {
        log.info("operation log {} : delete tag", projectId);
        try {
            List<String> taskIds = peTaskMapper.queryTaskIdsByTagId(tagId, workspaceId);
            if (!taskIds.isEmpty() || taskIds.size() > TAG_TASK_MAPPING) {
                return taskIds.size();
            }
            peTagMapper.deleteById(tagId, workspaceId);
            return TAG_TASK_MAPPING;
        } catch (Exception e) {
            LOGGER.error("Failed to delete tag, id={}", tagId, e);
            throw new AgentStudioException(StudioError.FAILED_TO_DELETE_TAG, tagId);
        }
    }

    /**
     * 查询标签列表并进行分页
     * @param projectId projectId
     * @return 返回结果列表
     */
    @Override
    @LimitOffset
    public TagListVo listTags(String projectId, ListTagsQo listTagsQo) {
        log.info("operation log {} : list tags", projectId);
        if (listTagsQo.getLimit() < 0 || listTagsQo.getOffset() < 0) {
            throw new AgentStudioException(StudioError.LIMIT_OR_OFFSET_INVALID);
        }
        PageInfo<TagVo> tagResult = queryWithLimitAndOffset(listTagsQo.getLimit(), listTagsQo.getOffset(), listTagsQo.getWorkspaceId());
        TagListVo pager = new TagListVo();
        pager.setData(tagResult.getList());
        pager.setHasNextPage(tagResult.isHasNextPage());
        pager.setCount(tagResult.getTotal());
        pager.setTotalPage(tagResult.getPages());
        return pager;
    }

    /**
     * 更新标签
     *
     * @param body body
     * @return 更新成功后的实体
     */

    public TagVo updateTag(String projectId, String tagId, String workspaceId, TagDto body) {
        log.info("operation log {} : update tag", projectId);
        List<String> taskIds = peTaskMapper.queryTaskIdsByTagId(tagId, workspaceId);
        if (!taskIds.isEmpty() || taskIds.size() > TAG_TASK_MAPPING) {
            throw new AgentStudioException(StudioError.FAILED_TO_UPDATE_TAG, tagId);
        }
        if (Boolean.TRUE.equals(peTagMapper.isExist(body.getName(), workspaceId))) {
            throw new AgentStudioException(StudioError.TAG_NAME_IS_EXIST);
        }
        try {
            peTagMapper.updateOneTag(PeTag.builder().id(tagId).name(body.getName()).workspaceId(workspaceId).build());
            List<PeTag> peTags = peTagMapper.queryByIds(Collections.singletonList(tagId), workspaceId);
            TagVo tagVo = new TagVo();
            BeanUtils.copyProperties(peTags.get(0), tagVo);
            return tagVo;
        } catch (Exception e) {
            LOGGER.error("Failed to update tag {} ", tagId, e);
            throw new AgentStudioException(StudioError.FAILED_TO_UPDATE_TAG, tagId);
        }
    }

    /**
     * 使用PageInfo获取查询结果及分页信息
     *
     * @param limit 数据条数
     * @param offset 数据偏移量
     * @return pageInfo
     */
    private PageInfo<TagVo> queryWithLimitAndOffset(int limit, int offset, String workspaceId) {
        try {
            PageInfo<PeTag> tagResult = PageHelper.offsetPage(offset, limit)
                .doSelectPageInfo(() -> this.peTagMapper.queryAll());
            PageInfo<TagVo> result = new PageInfo<>();
            BeanUtils.copyProperties(tagResult, result);
            return result;
        } catch (Exception e) {
            LOGGER.error("Failed to query tag list", e);
            throw new AgentStudioException(StudioError.FAILED_TO_QUERY_TAG);
        }
    }
}
