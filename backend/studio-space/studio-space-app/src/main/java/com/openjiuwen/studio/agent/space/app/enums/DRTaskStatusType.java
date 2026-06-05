package com.openjiuwen.studio.agent.space.app.enums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DeepResearch 任务状态枚举
 * 初始化
 * 0：未开始
 * <p>
 * 开始运行
 * 10：生成问题中
 * 20：等待用户回答问题
 * 30：开始生成研究报告
 * <p>
 * 已结束 状态不能被更改 updateStatusById 中校验
 * 40：正常结束
 * 50：手动停止
 * 60：任务删除
 * 70：执行失败，运行过程中异常
 * 80：运行超时
 */
public enum DRTaskStatusType {
    INITIATING(0),
    GENERATE_QUESTIONS(10),
    WAITING_USER_INPUT(20),
    PLAN_RUNNING(30),
    END(40),
    STOP(50),
    DELETED(60),
    FAIL(70),
    TIME_OUT(80),
    ;

    // 静态Map用于缓存status到枚举实例的映射
    private static final Map<Integer, DRTaskStatusType> LOOKUP = new HashMap<>();

    // 所有完成状态
    private static final Set<DRTaskStatusType> COMPLETED_STATUSES = Set.of(
        END,
        STOP,
        DELETED,
        FAIL,
        TIME_OUT
    );

    // 可停止状态
    private static final Set<DRTaskStatusType> STOPPABLE_STATUSES = Set.of(
        GENERATE_QUESTIONS,
        WAITING_USER_INPUT,
        PLAN_RUNNING
    );

    // 运行状态
    private static final Set<DRTaskStatusType> RUNNING_STATUSES = Set.of(
        GENERATE_QUESTIONS,
        PLAN_RUNNING
    );

    private static final List<DRTaskStatusType> RUNNING_STATUSES_LIST = List.copyOf(RUNNING_STATUSES);

    private static final List<DRTaskStatusType> COMPLETED_STATUSES_LIST = List.copyOf(COMPLETED_STATUSES);

    // 静态初始化块，在类加载时执行
    static {
        for (DRTaskStatusType type : values()) {
            LOOKUP.put(type.getStatus(), type);
        }
    }

    private final int status;

    DRTaskStatusType(int status) {
        this.status = status;
    }

    // 通过status获取对应的枚举实例
    public static DRTaskStatusType getByStatus(int status) {
        DRTaskStatusType type = LOOKUP.get(status);
        if (type == null) {
            throw new IllegalArgumentException("[getByStatus] Invalid DRTaskStatusType status: " + status);
        }
        return type;
    }

    public static boolean isCompleted(DRTaskStatusType type) {
        return COMPLETED_STATUSES.contains(type);
    }

    public static boolean canStop(DRTaskStatusType type) {
        return STOPPABLE_STATUSES.contains(type);
    }

    public static boolean canChat(DRTaskStatusType type) {
        return !isCompleted(type);
    }

    public static boolean isRunning(DRTaskStatusType type) {
        return RUNNING_STATUSES.contains(type);
    }

    public static List<DRTaskStatusType> getRunningStatus() {
        return RUNNING_STATUSES_LIST;
    }

    public static List<DRTaskStatusType> getCompletedStatus() {
        return COMPLETED_STATUSES_LIST;
    }

    public int getStatus() {
        return status;
    }
}
