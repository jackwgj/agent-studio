/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.constant;

/**
 * prompt路径
 *
 */
public interface PromptPath {
    /**
     * 生成开场白prompt
     */
    String PROLOGUE_PROMPT = "prompts/prologue_generation_prompt.pt";

    /**
     * 生成英文开场白prompt
     */
    String PROLOGUE_PROMPT_EN = "prompts/prologue_generation_prompt_en.pt";

    /**
     * 生成智能体描述prompt
     */
    String DESCRIPTION_PROMPT = "prompts/description_generation_prompt.pt";

    /**
     * 生成英文智能体描述prompt
     */
    String DESCRIPTION_PROMPT_EN = "prompts/description_generation_prompt_en.pt";

    /**
     * 生成智能体名字prompt
     */
    String NAME_PROMPT = "prompts/name_generation_prompt.pt";

    /**
     * 生成英文智能体名字prompt
     */
    String NAME_PROMPT_EN = "prompts/name_generation_prompt_en.pt";

    /**
     * 智能生成智能体icon prompt
     */
    String ICON_PROMPT = "prompts/icon_generation_prompt.pt";

    /**
     * 智能生成英文智能体icon prompt
     */
    String ICON_PROMPT_EN = "prompts/icon_generation_prompt_en.pt";

    /**
     * 生成推荐问prompt
     */
    String RECOMMENDED_QUESTIONS_PROMPT = "prompts/recommended_questions_generation_prompt.pt";

    /**
     * 生成英文推荐问prompt
     */
    String RECOMMENDED_QUESTIONS_PROMPT_EN = "prompts/recommended_questions_generation_prompt_en.pt";

    /**
     * 意图类别prompt
     */
    String CLASSIFICATION_CATEGORY_PROMPT = "prompts/workflow_classification_category.pt";

    /**
     * 意图分类prompt
     */
    String CLASSIFICATION_PROMPT = "prompts/workflow_classification_prompt.pt";

    /**
     * 代码节点模板
     */
    String CODE_PT = "template/workflow_code_template.template";

    /**
     * 大模型节点用于解析特定返回schema的prompt路径
     */
    String LLM_JSON_PROMPT = "prompts/workflow_llm_json_prompt.pt";

    /**
     * JSON描述格式prompt路径
     */
    String LLM_JSON_DESC = "prompts/workflow_llm_json_desc.pt";

    /**
     * 智能添加插件prompt
     */
    String AUTO_ADD_TOOLS_PROMPT = "prompts/auto_add_tools_prompt.pt";

    /**
     * 智能添加skill prompt
     */
    String AUTO_ADD_SKILLS_PROMPT = "prompts/auto_add_skills_prompt.pt";

    /**
     * 智能添加skill prompt
     */
    String AUTO_ADD_SKILLS_PROMPT_EN = "prompts/auto_add_skills_prompt_en.pt";

    /**
     * 生成引用
     */
    String AUTO_GENERATION_REFERENCES = "prompts/auto_generation_references.pt";

    /**
     * 保留召回图片标签
     */
    String PRESERVE_RETRIEVE_IMG = "prompts/preserve_img_in_retrieval.pt";

    /**
     * 保留召回图片标签（英文）
     */
    String PRESERVE_RETRIEVE_IMG_EN = "prompts/preserve_img_in_retrieval_en.pt";

    /**
     * 智能生成代码
     */
    String CODE_PROMPT = "prompts/code_generation_prompt.pt";

    /**
     * 智能生成代码 英文
     */
    String CODE_PROMPT_EN = "prompts/code_generation_prompt_en.pt";

    /**
     * 引用和归属框架模板
     */
    String REFERENCE_TEMPLATE = "prompts/reference_template.pt";

    /**
     * 引用和归属模板
     */
    String REFERENCE = "prompts/reference.pt";

    /**
     * 引用和归属模板(JSON格式化模板)
     */
    String REFERENCE_JSON_FORMATE = "prompts/reference_json_formate.pt";

    /**
     * 引用和归属框架模板（英文）
     */
    String REFERENCE_TEMPLATE_EN = "prompts/reference_template_en.pt";

    /**
     * 引用和归属模板（英文）
     */
    String REFERENCE_EN = "prompts/reference_en.pt";

    /**
     * 引用和归属模板(JSON格式化模板)（英文）
     */
    String REFERENCE_JSON_FORMATE_EN = "prompts/reference_json_formate_en.pt";
}
