package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class ChunkResp {
    /**
     * 对话id，对应task_id
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 阶段序号
     */
    @JsonProperty("section_idx")
    private String sectionIdx;

    /**
     * 消息发送者的角色
     * "entry","generate_questions","feedback_handler","outline", "plan_reasoning","sub_reporter","collector_supervisor","collector_summary", "end"
     */
    private String agent;

    /**
     * 每一条消息的唯一id，流式消息通过这个字段进行聚合
     */
    @JsonProperty("message_id")
    private String messageId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     * "message_chunk", "interrupt"
     */
    @JsonProperty("message_type")
    private String messageType;

    /**
     * 消息的事件类型
     * "start","done","message","summary_response", "waiting_user_input"
     */
    private String event;

    /**
     * 创建时间
     */
    @JsonProperty("created_time")
    private Timestamp createdTime;

    /**
     * 耗时信息
     */
    private Latency latency;
}
