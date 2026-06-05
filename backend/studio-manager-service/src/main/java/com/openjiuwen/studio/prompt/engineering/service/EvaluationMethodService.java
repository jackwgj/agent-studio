/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvalResult;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.service.model.ModelServiceCollection;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.vo.ResultNumCount;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 评估方法service，和评估任务解耦
 *
 */
@Component
public class EvaluationMethodService {
    private static final int CORRECT_NUM = 100;

    private static final int FULL_SCORE = 100;

    private static final double MATH_LIMIT = 1e-8d;

    private static final String MODEL_EVA_PROMPT =
        "我将提供两条内容给你，请根据两条内容的相似度给出评分，要求评分是0到100之间的数字，只回复评分数字即可，不要解释原因，不要回复其他信息。\n"
            + "内容1：{{promptAnswer}}\n" + "内容2：{{standardAnswer}}";

    @Resource
    private ModelServiceCollection modelServiceCollection;

    /**
     * 评估
     *
     * @param promptAnswer promptAnswer
     * @param standardAnswer standardAnswer
     * @return 分数
     */
    public Integer evaluate(JobInstance jobInstance, String promptAnswer, String standardAnswer,
        PeEvaluationTask peEvaluationTask) {
        int result = 0;
        if (StringUtils.isEmpty(promptAnswer) || StringUtils.isEmpty(standardAnswer)) {
            return result;
        }
        String evalMethod = peEvaluationTask.getMethod().getMethod();
        switch (evalMethod) {
            case "matching":
                result = Objects.equals(promptAnswer, standardAnswer) ? FULL_SCORE : 0;
                break;
            case "regular_matching":
                String regularExpression = peEvaluationTask.getRegularExpression();
                result = Pattern.matches(regularExpression, promptAnswer) ? FULL_SCORE : 0;
                break;
            case "model":
                ModelConfig evalModelConfig = peEvaluationTask.getEvalModelConfig();
                result = getModelEvaluationScore(jobInstance, evalModelConfig, promptAnswer, standardAnswer);
                break;
            case "similarity":
                // 分词并构建词频向量
                Map<String, Integer> prediction = buildFrequencyVector(promptAnswer);
                Map<String, Integer> correct = buildFrequencyVector(standardAnswer);

                // 计算余弦相似度（适应百分制）
                double similarity = calculateCosineSimilarity(prediction, correct);
                result = (int) Math.round(similarity * FULL_SCORE);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 构建词频向量
     *
     * @param str str
     * @return 词频向量
     */
    private Map<String, Integer> buildFrequencyVector(String str) {
        List<Term> terms = HanLP.segment(str);
        Map<String, Integer> termFrequencyVector = new HashMap<>();

        for (Term term : terms) {
            String word = term.word;
            termFrequencyVector.put(word, termFrequencyVector.getOrDefault(word, 0) + 1);
        }

        return termFrequencyVector;
    }

    /**
     * 计算余弦相似度
     *
     * @param vec1 vec1
     * @param vec2 vec2
     * @return 余弦相似度
     */
    private double calculateCosineSimilarity(Map<String, Integer> vec1, Map<String, Integer> vec2) {
        // 计算内积
        double dotProduct = 0.00d;
        for (Map.Entry<String, Integer> vec : vec1.entrySet()) {
            String vecTerm = vec.getKey();
            Integer vecValue = vec.getValue();
            if (vec2.containsKey(vecTerm)) {
                dotProduct += vecValue * vec2.get(vecTerm);
            }
        }

        // 计算向量长度
        double magnitude1 = calculateVectorMagnitude(vec1);
        double magnitude2 = calculateVectorMagnitude(vec2);

        // 计算余弦相似度
        if (magnitude1 > MATH_LIMIT && magnitude2 > MATH_LIMIT) {
            return dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.00;
        }
    }

    /**
     * 计算向量长度
     *
     * @param vector vector
     * @return 向量长度
     */
    private double calculateVectorMagnitude(Map<String, Integer> vector) {
        double sum = 0.00d;
        for (int frequency : vector.values()) {
            sum += frequency * frequency;
        }
        return Math.sqrt(sum);
    }

    /**
     * 获取模型评估的得分
     */
    private int getModelEvaluationScore(JobInstance jobInstance, ModelConfig evalModelConfig, String promptAnswer,
        String standardAnswer) {
        PromptBody promptBody = new PromptBody();
        promptBody.setQuestion(MODEL_EVA_PROMPT);
        promptBody.setModelType(StringUtils.isEmpty(evalModelConfig.getModelSource())
            ? CommonConstant.AGENT_BUILDER_MODEL_TYPE
            : evalModelConfig.getModelSource());

        // 构建模型评估prompt变量
        List<VariableDto> variableDtos = new ArrayList<>();
        VariableDto promptAnswerVariable = new VariableDto();
        promptAnswerVariable.setKey("promptAnswer");
        promptAnswerVariable.setValue(promptAnswer);
        variableDtos.add(promptAnswerVariable);
        VariableDto standardAnswerVariable = new VariableDto();
        standardAnswerVariable.setKey("standardAnswer");
        standardAnswerVariable.setValue(standardAnswer);
        variableDtos.add(standardAnswerVariable);
        promptBody.setVariables(variableDtos);

        // 构建模型评估参数配置
        evalModelConfig.setTemperature(0.1);
        evalModelConfig.setPresencePenalty(0.0);
        evalModelConfig.setMaxTokens(2048);
        promptBody.setModelConfig(evalModelConfig);

        // 调用大模型接口
        InvokeResp invokeResp = modelServiceCollection.callModel(jobInstance.getWorkspaceId(), promptBody,
            RequestContextUtils.getRequestAuthToken(), RequestContextUtils.getRequestProjectId());
        return StringUtils.isNumeric(invokeResp.getResult()) ? Integer.parseInt(invokeResp.getResult()) : 0;
    }

    /**
     * 获取每个评估任务对应的prompt的分数
     *
     * @param evaluationResults 评估任务结果
     * @return 估任务对应的prompt的分数表
     */
    public Map<String, Integer> graspPromptScore(List<PeEvaluationResult> evaluationResults) {
        try {
            Map<String, Integer> promptScoreMap = new HashMap<>();
            Map<String, Integer> promptScoreSum = new HashMap<>();
            Map<String, Integer> promptScoreCount = new HashMap<>();
            for (PeEvaluationResult result : evaluationResults) {
                PeEvalResult evalResult = result.getEvalResult();
                String promptId = result.getPromptId();
                int promptScore = evalResult.getScore();
                int promptSum = promptScoreSum.containsKey(promptId)
                    ? promptScoreSum.get(promptId) + promptScore
                    : promptScore;
                int promptCount = promptScoreCount.containsKey(promptId) ? promptScoreCount.get(promptId) + 1 : 1;
                promptScoreSum.put(promptId, promptSum);
                promptScoreCount.put(promptId, promptCount);
                promptScore = promptSum / promptCount;
                promptScoreMap.put(promptId, promptScore);
            }
            return promptScoreMap;
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.QUERY_EVALUATION_RESULT_EXCEPTION);
        }
    }

    /**
     * 解析每个评估任务对应的prompt的结果
     *
     * @param evaluationResults 评估任务结果
     * @return 估任务对应的prompt的结果表
     */
    public Map<String, ResultNumCount> parsePromptResult(List<PeEvaluationResult> evaluationResults) {
        Map<String, ResultNumCount> promptResultMap = new HashMap<>();
        Map<String, Integer> promptScoreSum = new HashMap<>();
        Map<String, Integer> promptTokenSum = new HashMap<>();
        Map<String, Double> promptTimeSum = new HashMap<>();
        Map<String, Integer> promptCount = new HashMap<>();

        for (PeEvaluationResult result : evaluationResults) {
            PeEvalResult evalResult = result.getEvalResult();
            String promptId = result.getPromptId();
            if (!promptResultMap.containsKey(promptId)) {
                promptResultMap.put(promptId, new ResultNumCount());
            }
            int promptSum = promptCount.containsKey(promptId) ? promptCount.get(promptId) + 1 : 1;
            promptCount.put(promptId, promptSum);

            // 计算平均分
            int promptScore = evalResult.getScore();
            int scoreSum = promptScoreSum.containsKey(promptId)
                ? promptScoreSum.get(promptId) + promptScore
                : promptScore;
            promptScoreSum.put(promptId, scoreSum);
            promptResultMap.get(promptId).setScore(scoreSum / promptSum);

            // 计算平均消耗token
            int promptToken = evalResult.getToken();
            int tokenSum = promptTokenSum.containsKey(promptId)
                ? promptTokenSum.get(promptId) + promptToken
                : promptToken;
            promptTokenSum.put(promptId, tokenSum);
            promptResultMap.get(promptId).setToken(tokenSum / promptSum);

            // 计算平均时间
            double promptTime = evalResult.getTime();
            double timeSum = promptTimeSum.containsKey(promptId)
                ? promptTimeSum.get(promptId) + promptTime
                : evalResult.getTime();
            promptTimeSum.put(promptId, timeSum);
            promptResultMap.get(promptId).setTime(timeSum / promptSum);

            // 计算答案正确数量
            if (promptScore == CORRECT_NUM) {
                promptResultMap.get(promptId).setCorrect(promptResultMap.get(promptId).getCorrect() + 1);
            }
        }
        return promptResultMap;
    }
}
