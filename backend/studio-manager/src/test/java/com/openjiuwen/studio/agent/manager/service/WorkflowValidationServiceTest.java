/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Transactional
public class WorkflowValidationServiceTest extends BaseTest {
    @Autowired
    WorkflowValidationService workflowValidationService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
    }

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(workflowValidationService, "obsService", obsService);
    }

    @Test
    void testWorkflowConfigsValidation() {
        String configStr =
            "{\"content_review\":{\"enable\":true,\"filter\":{\"keywords\":\"keyword1,keyword2\"},\"replace\":[{\"keywords\":\"keyword1,keyword2\",\"replace\":\"替换词\"}],\"reply\":[{\"keywords\":\"keyword1,keyword2\",\"input_enable\":true,\"input_text\":\"输入兜底回复\",\"output_enable\":true,\"output_text\":\"输出兜底回复\"}]}}";
        Map<String, Object> configMap = JsonUtils.json2Obj(configStr,
            com.openjiuwen.studio.agent.common.constant.Constants.MAP_TYPE_REFERENCE);
        // 抛出敏感词重复异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            workflowValidationService.validateWorkflowConfigs(configMap);
        });
    }

    @Test
    void testWorkflowConfigsReplaceValidation() {
        String configStr =
            "{\"content_review\":{\"enable\":true,\"filter\":{\"keywords\":\"keyword1,keyword2\"},\"replace\":[{\"keywords\":\"keyword1,keyword2\",\"replace\":\"01 事件起因：家长投诉课时严重不足事情发生在深圳市龙岗区某小学。据投诉家长反映，该校二年级每周仅有一节英语课，与同年级其他学校的3节、三至四年级的4节相比，存在明显差距。甚至连同为语言类学科的语文（每周7节）也形成了强烈反差。家长担忧，作为需要持续输入的語言学科，一周一节根本无法保证学习效果。家长还提到，学校今年9月开学后突然取消了二年级英语学科的知能教辅资料，且明确禁止学生将该资料带入校园。这意味着课堂巩固环节的缺失，学习责任转向了家庭。2 师资问题：频繁更换教师加剧困境除课时不足外，英语老师“代课杂乱”问题也困扰着家长。据描述，孩子二年级开学至今仅两个月，已经先后有3位老师带过英语课“第一位老师教了3周后说要去培训，换了第二位老师；第二位老师带了1个月，又说要兼顾其他年级，现在换成了第三位老师。”家长担忧，频繁更换教师会导致教学内容碎片化，影响孩子对英语学科的兴趣和基础能力培养。这种情况可能暴露了教育资源分配的问题。一位有10年小学英语教学经验的教师分析，这可能与学校英语师资紧张有关。03 官方回应：符合规定，学校有自主权9月8日，龙岗区教育局对投诉作出了公开回应。官方明确指出，根据国家义务教育课程方案，英语从三年级才正式开设。龙岗区结合一二年级英语教材《英语口语交际》的教学要求，建议小学一二年级可开设以听说为主的语地方课程，每周1课时；三至六年级每周3课时。因此，学校目前的安排符合规定。对于不同学校之间的英语课时差异，教育局解释，深圳市政策允许各校在周总课时不变的前提下，灵活安排学科课时。这意味着部分学校可能根据自身教学资源和特色增加英语课时。04 教辅资料问题：严格执行“双减”政策针对家长提出的教辅资料问题，龙岗区教育局表示，为深入贯彻落实“双减”政策，推进教辅材料规范管理，切实减轻学生过重课业负担和家长经济负担广东省教育厅近期印发了《关于进一步规范全省中小学教辅材料进校园管理的通知》，核心是严格执行“一科一辅”要求。由于小学一、二年级不布置书面家庭作业，各学科教材中已有基本的课后练习，能满足学生巩固需求，故一二年级没必要使用教辅材料，也严禁学校或教师推荐课后教辅材料。05更大背景：多地缩减英语课时趋势深圳这起争议并非孤立事件。今年以来，多地都在调整英语课时安排。2025年7月，广州市教育局发布《关于印发义务教育课程计划（2025年修订）的通知》，其中几条变化特别引人关注。广州方案明确：小学一二年级每周总课时从28节减到26节；一二年级英语口语课调整为每周1-2节；初中七、八、九年级英语课统一减至每周4节。这些调整在教育圈引发了激烈讨论。一方面，政策层面确实在努力为学生减负；另一方面，英语在中高考中的分值却不降反升。广州市刚刚公布了最新的中考分数构成：从2027年起，语文和英语从120分上调至140分，数学上调到150分。\"}],\"reply\":[{\"keywords\":\"keyword1,keyword2\",\"input_enable\":true,\"input_text\":\"输入兜底回复\",\"output_enable\":true,\"output_text\":\"输出兜底回复\"}]}}";
        Map<String, Object> configMap = JsonUtils.json2Obj(configStr,
            com.openjiuwen.studio.agent.common.constant.Constants.MAP_TYPE_REFERENCE);
        // 抛出替换词异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            workflowValidationService.validateWorkflowConfigs(configMap);
        });
    }

    @Test
    void testWorkflowConfigsInputValidation() {
        String configStr =
            "{\"content_review\":{\"enable\":true,\"filter\":{\"keywords\":\"keyword1,keyword2\"},\"replace\":[{\"keywords\":\"keyword1,keyword2\",\"replace\":\"替换词\"}],\"reply\":[{\"keywords\":\"keyword1,keyword2\",\"input_enable\":true,\"input_text\":\"01 事件起因：家长投诉课时严重不足事情发生在深圳市龙岗区某小学。据投诉家长反映，该校二年级每周仅有一节英语课，与同年级其他学校的3节、三至四年级的4节相比，存在明显差距。甚至连同为语言类学科的语文（每周7节）也形成了强烈反差。家长担忧，作为需要持续输入的語言学科，一周一节根本无法保证学习效果。家长还提到，学校今年9月开学后突然取消了二年级英语学科的知能教辅资料，且明确禁止学生将该资料带入校园。这意味着课堂巩固环节的缺失，学习责任转向了家庭。2 师资问题：频繁更换教师加剧困境除课时不足外，英语老师“代课杂乱”问题也困扰着家长。据描述，孩子二年级开学至今仅两个月，已经先后有3位老师带过英语课“第一位老师教了3周后说要去培训，换了第二位老师；第二位老师带了1个月，又说要兼顾其他年级，现在换成了第三位老师。”家长担忧，频繁更换教师会导致教学内容碎片化，影响孩子对英语学科的兴趣和基础能力培养。这种情况可能暴露了教育资源分配的问题。一位有10年小学英语教学经验的教师分析，这可能与学校英语师资紧张有关。03 官方回应：符合规定，学校有自主权9月8日，龙岗区教育局对投诉作出了公开回应。官方明确指出，根据国家义务教育课程方案，英语从三年级才正式开设。龙岗区结合一二年级英语教材《英语口语交际》的教学要求，建议小学一二年级可开设以听说为主的语地方课程，每周1课时；三至六年级每周3课时。因此，学校目前的安排符合规定。对于不同学校之间的英语课时差异，教育局解释，深圳市政策允许各校在周总课时不变的前提下，灵活安排学科课时。这意味着部分学校可能根据自身教学资源和特色增加英语课时。04 教辅资料问题：严格执行“双减”政策针对家长提出的教辅资料问题，龙岗区教育局表示，为深入贯彻落实“双减”政策，推进教辅材料规范管理，切实减轻学生过重课业负担和家长经济负担广东省教育厅近期印发了《关于进一步规范全省中小学教辅材料进校园管理的通知》，核心是严格执行“一科一辅”要求。由于小学一、二年级不布置书面家庭作业，各学科教材中已有基本的课后练习，能满足学生巩固需求，故一二年级没必要使用教辅材料，也严禁学校或教师推荐课后教辅材料。05更大背景：多地缩减英语课时趋势深圳这起争议并非孤立事件。今年以来，多地都在调整英语课时安排。2025年7月，广州市教育局发布《关于印发义务教育课程计划（2025年修订）的通知》，其中几条变化特别引人关注。广州方案明确：小学一二年级每周总课时从28节减到26节；一二年级英语口语课调整为每周1-2节；初中七、八、九年级英语课统一减至每周4节。这些调整在教育圈引发了激烈讨论。一方面，政策层面确实在努力为学生减负；另一方面，英语在中高考中的分值却不降反升。广州市刚刚公布了最新的中考分数构成：从2027年起，语文和英语从120分上调至140分，数学上调到150分。\",\"output_enable\":true,\"output_text\":\"输出兜底回复\"}]}}";
        Map<String, Object> configMap = JsonUtils.json2Obj(configStr,
            com.openjiuwen.studio.agent.common.constant.Constants.MAP_TYPE_REFERENCE);
        // 抛出替换词异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            workflowValidationService.validateWorkflowConfigs(configMap);
        });
    }

    @Test
    void testWorkflowConfigsOutputValidation() {
        String configStr =
            "{\"content_review\":{\"enable\":true,\"filter\":{\"keywords\":\"keyword1,keyword2\"},\"replace\":[{\"keywords\":\"keyword1,keyword2\",\"replace\":\"替换词\"}],\"reply\":[{\"keywords\":\"keyword1,keyword2\",\"input_enable\":true,\"input_text\":\"输入兜底回复\",\"output_enable\":true,\"output_text\":\"01 事件起因：家长投诉课时严重不足事情发生在深圳市龙岗区某小学。据投诉家长反映，该校二年级每周仅有一节英语课，与同年级其他学校的3节、三至四年级的4节相比，存在明显差距。甚至连同为语言类学科的语文（每周7节）也形成了强烈反差。家长担忧，作为需要持续输入的語言学科，一周一节根本无法保证学习效果。家长还提到，学校今年9月开学后突然取消了二年级英语学科的知能教辅资料，且明确禁止学生将该资料带入校园。这意味着课堂巩固环节的缺失，学习责任转向了家庭。2 师资问题：频繁更换教师加剧困境除课时不足外，英语老师“代课杂乱”问题也困扰着家长。据描述，孩子二年级开学至今仅两个月，已经先后有3位老师带过英语课“第一位老师教了3周后说要去培训，换了第二位老师；第二位老师带了1个月，又说要兼顾其他年级，现在换成了第三位老师。”家长担忧，频繁更换教师会导致教学内容碎片化，影响孩子对英语学科的兴趣和基础能力培养。这种情况可能暴露了教育资源分配的问题。一位有10年小学英语教学经验的教师分析，这可能与学校英语师资紧张有关。03 官方回应：符合规定，学校有自主权9月8日，龙岗区教育局对投诉作出了公开回应。官方明确指出，根据国家义务教育课程方案，英语从三年级才正式开设。龙岗区结合一二年级英语教材《英语口语交际》的教学要求，建议小学一二年级可开设以听说为主的语地方课程，每周1课时；三至六年级每周3课时。因此，学校目前的安排符合规定。对于不同学校之间的英语课时差异，教育局解释，深圳市政策允许各校在周总课时不变的前提下，灵活安排学科课时。这意味着部分学校可能根据自身教学资源和特色增加英语课时。04 教辅资料问题：严格执行“双减”政策针对家长提出的教辅资料问题，龙岗区教育局表示，为深入贯彻落实“双减”政策，推进教辅材料规范管理，切实减轻学生过重课业负担和家长经济负担广东省教育厅近期印发了《关于进一步规范全省中小学教辅材料进校园管理的通知》，核心是严格执行“一科一辅”要求。由于小学一、二年级不布置书面家庭作业，各学科教材中已有基本的课后练习，能满足学生巩固需求，故一二年级没必要使用教辅材料，也严禁学校或教师推荐课后教辅材料。05更大背景：多地缩减英语课时趋势深圳这起争议并非孤立事件。今年以来，多地都在调整英语课时安排。2025年7月，广州市教育局发布《关于印发义务教育课程计划（2025年修订）的通知》，其中几条变化特别引人关注。广州方案明确：小学一二年级每周总课时从28节减到26节；一二年级英语口语课调整为每周1-2节；初中七、八、九年级英语课统一减至每周4节。这些调整在教育圈引发了激烈讨论。一方面，政策层面确实在努力为学生减负；另一方面，英语在中高考中的分值却不降反升。广州市刚刚公布了最新的中考分数构成：从2027年起，语文和英语从120分上调至140分，数学上调到150分。\"}]}}";
        Map<String, Object> configMap = JsonUtils.json2Obj(configStr,
            com.openjiuwen.studio.agent.common.constant.Constants.MAP_TYPE_REFERENCE);
        // 抛出替换词异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            workflowValidationService.validateWorkflowConfigs(configMap);
        });
    }
}
