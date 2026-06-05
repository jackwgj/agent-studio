package com.openjiuwen.studio.agent.space.app.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.ChunkResp;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.EndContent;
import com.openjiuwen.studio.agent.space.app.task.DRTaskHandler;
import com.openjiuwen.studio.agent.space.app.util.FileTransferUtils;
import com.openjiuwen.studio.agent.space.app.util.AgentBuilderTransformUtil;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DRHandleFileCreationTest {
    @Mock
    private AgentBuilderDispatchVo AgentBuilderDispatchVo;

    @Mock
    private FileTransferUtils fileTransferUtil;

    @Mock
    private AgentBuilderTransformUtil AgentBuilderTransformUtil;

    @Mock
    private AgentBuilderFileMapper AgentBuilderFileMapper;

    @Mock
    private DRTaskHandler drTaskHandler;

    private DREventSourceListener dREventSourceListener;

    @Test
    void testHandleFileCreation1() {
        // Given 设置测试对象
        dREventSourceListener = new DREventSourceListener(AgentBuilderDispatchVo, () -> {});

        // 硬编码JSON内容
        String fileContent
            = "{'response_content': '# 石头科技有限公司客户尽职调查报告\\n\\n# 摘要\\n\\n石头科技2025年营收达186.16亿元', \"id\": 1, \"warning_info\": \"\\n[211801] section_idx: 1 | [ResearchPlanReasoningNode]  plan_executed_num = 2 Error when Planner generate a plan, error: section_idx: 1 | Round 2/2 | Error when Planner generating a plan. retry (3/3).error: [181001] model call failed, reason: openAI API async stream error: Error code: 403 - {'error_code': 'AgentBuilder.02501049', 'error_msg': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_reason': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_suggestion': '请查看请求响应查询详细原因', 'details': [{'error_msg': '{\\\"error\\\":{\\\"code\\\":\\\"ModelArts.81101\\\",\\\"message\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"param\\\":null,\\\"type\\\":\\\"TooManyRequests\\\"},\\\"error_code\\\":\\\"ModelArts.81101\\\",\\\"error_msg\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"span_id\\\":\\\"b67334754843debbefb51c31166bf19e\\\"}'}]}\\n[212000] section_idx: 1 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:22, classified_content_num:0[211801] section_idx: 2 | [ResearchPlanReasoningNode]  plan_executed_num = 2 Error when Planner generate a plan, error: section_idx: 2 | Round 2/2 | Error when Planner generating a plan. retry (3/3).error: [181001] model call failed, reason: openAI API async stream error: Error code: 403 - {'error_code': 'AgentBuilder.02501049', 'error_msg': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_reason': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_suggestion': '请查看请求响应查询详细原因', 'details': [{'error_msg': '{\\\"error\\\":{\\\"code\\\":\\\"ModelArts.81101\\\",\\\"message\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"param\\\":null,\\\"type\\\":\\\"TooManyRequests\\\"},\\\"error_code\\\":\\\"ModelArts.81101\\\",\\\"error_msg\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"span_id\\\":\\\"4bdc1544c882ebe835c851e5868cbae6\\\"}'}]}\\n[212000] section_idx: 2 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:21, classified_content_num:0\", \"exception_info\": \"\\n[212000] section_idx: 1 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:22, classified_content_num:0[212000] section_idx: 2 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:21, classified_content_num:0\"}";

        ChunkResp chunkResp = createChunkRespWithEndContent(fileContent);

        // When & Then: 断言抛出空指针异常
        assertThrows(NullPointerException.class, () -> {
            dREventSourceListener.handleFileCreation(chunkResp, AgentBuilderDispatchVo, "test-report");
        });
    }

    @Test
    void testHandleFileCreation2() {
        // Given 设置测试对象
        dREventSourceListener = new DREventSourceListener(AgentBuilderDispatchVo, () -> {});

        // 硬编码JSON内容
        String fileContent
            = "{'response_content': '', \"id\": 1, \"warning_info\": \"\\n[211801] section_idx: 1 | [ResearchPlanReasoningNode]  plan_executed_num = 2 Error when Planner generate a plan, error: section_idx: 1 | Round 2/2 | Error when Planner generating a plan. retry (3/3).error: [181001] model call failed, reason: openAI API async stream error: Error code: 403 - {'error_code': 'AgentBuilder.02501049', 'error_msg': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_reason': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_suggestion': '请查看请求响应查询详细原因', 'details': [{'error_msg': '{\\\"error\\\":{\\\"code\\\":\\\"ModelArts.81101\\\",\\\"message\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"param\\\":null,\\\"type\\\":\\\"TooManyRequests\\\"},\\\"error_code\\\":\\\"ModelArts.81101\\\",\\\"error_msg\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"span_id\\\":\\\"b67334754843debbefb51c31166bf19e\\\"}'}]}\\n[212000] section_idx: 1 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:22, classified_content_num:0[211801] section_idx: 2 | [ResearchPlanReasoningNode]  plan_executed_num = 2 Error when Planner generate a plan, error: section_idx: 2 | Round 2/2 | Error when Planner generating a plan. retry (3/3).error: [181001] model call failed, reason: openAI API async stream error: Error code: 403 - {'error_code': 'AgentBuilder.02501049', 'error_msg': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_reason': '调用第三方模型服务失败,请查看请求响应查询详细原因', 'error_suggestion': '请查看请求响应查询详细原因', 'details': [{'error_msg': '{\\\"error\\\":{\\\"code\\\":\\\"ModelArts.81101\\\",\\\"message\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"param\\\":null,\\\"type\\\":\\\"TooManyRequests\\\"},\\\"error_code\\\":\\\"ModelArts.81101\\\",\\\"error_msg\\\":\\\"Too many requests, the rate limit is 500000 tokens per minute.\\\",\\\"span_id\\\":\\\"4bdc1544c882ebe835c851e5868cbae6\\\"}'}]}\\n[212000] section_idx: 2 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:21, classified_content_num:0\", \"exception_info\": \"\\n[212000] section_idx: 1 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:22, classified_content_num:0[212000] section_idx: 2 | [SubReporterNode]  Error when generate sub report, error: classify_doc_infos fail, doc_infos_num:21, classified_content_num:0\"}";

        ChunkResp chunkResp = createChunkRespWithEndContent(fileContent);

        // When & Then: 断言抛出空指针异常
        assertThrows(NullPointerException.class, () -> {
            dREventSourceListener.handleFileCreation(chunkResp, AgentBuilderDispatchVo, "test-report");
        });
    }

    private ChunkResp createChunkRespWithEndContent(String endContentString) {
        ChunkResp chunkResp = new ChunkResp();
        chunkResp.setSectionIdx("0");
        chunkResp.setAgent("end");
        chunkResp.setEvent("summary_response");
        chunkResp.setContent(endContentString);
        return chunkResp;
    }

    @Test
    void testWarningInfoInEndContent() {
        // 专门测试warning_info字段在EndContent中的正确处理
        EndContent endContent = new EndContent();

        // 测试设置和获取warning_info
        String warningMessage = "Warning: Memory usage exceeded 80%";
        endContent.setWarningInfo(warningMessage);
        assertEquals(warningMessage, endContent.getWarningInfo());

        // 测试无告警信息的情况
        endContent.setWarningInfo("");
        assertTrue(endContent.getWarningInfo().isEmpty());

        // 测试null值
        endContent.setWarningInfo(null);
        assertNull(endContent.getWarningInfo());
    }
}