package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderFileVo;
import com.openjiuwen.studio.agent.space.api.vo.response.file.MsgFileInfo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * DR 消息信息传输类
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DRMessageVo {
    /**
     * 消息id
     */
    @JsonProperty("id")
    private String id;

    /**
     * 父消息Id
     */
    @JsonProperty("parent_id")
    private String parentId;

    /**
     * 关联的任务id
     */
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 消息类型
     * 1：用户；2：系统
     * 用户不能传
     */
    @JsonProperty("type")
    private Integer type;

    /**
     * 消息内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 步骤列表
     */
    @JsonProperty("chunks")
    private List<ChunkResp> chunks;

    /**
     * 文件id列表
     */
    @JsonProperty("file_list")
    private List<AgentBuilderFileVo> fileList;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private long createTime;

    /**
     * 用户评分
     */
    @JsonProperty("rating")
    private int rating;

    @JsonProperty("mag_file_infos")
    private List<MsgFileInfo> msgFileInfos;
}
