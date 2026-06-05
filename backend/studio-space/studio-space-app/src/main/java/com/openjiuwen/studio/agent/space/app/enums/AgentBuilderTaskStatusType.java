package com.openjiuwen.studio.agent.space.app.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务状态枚举
 * 0：未开始；1：运行中；2：等待用户确认计划；3：触发暂停；4：暂停，等待用户输入；5：任务正常结束，等待用户输入；6：任务删除；7：任务完成，寿终正寝；8：运行失败，意外身亡；9：触发手动终止；10：手动终止完成；
 * 其中0是初始化，1\3是运行中，2\4\5是暂停态，6-10是终止态，其中9后会直接干掉容器，不管容器的状态不会再落库新数据，所以也认为是终止态
 */
public enum AgentBuilderTaskStatusType {
    INITIATING(0),
    RUNNING(1),
    WAITING_CONFIRM_PLAN(2),
    TO_PAUSE(3),
    WAITING_FOR_INPUT_WITH_TASK_PAUSED(4),
    WAITING_FOR_INPUT_WITH_TASK_COMPLETED(5),
    DELETED(6),
    END(7),
    FAIL(8),
    TO_STOP(9),
    STOP(10),
    ;

    // 静态Map用于缓存status到枚举实例的映射
    private static final Map<Integer, AgentBuilderTaskStatusType> LOOKUP = new HashMap<>();

    private final int status;

    AgentBuilderTaskStatusType(int status) {
        this.status = status;
    }

    // 静态初始化块，在类加载时执行
    static {
        for (AgentBuilderTaskStatusType type : values()) {
            LOOKUP.put(type.getStatus(), type);
        }
    }

    public int getStatus() {
        return status;
    }

    // 通过status获取对应的枚举实例
    public static AgentBuilderTaskStatusType getByStatus(int status) {
        return LOOKUP.get(status);
    }
}
