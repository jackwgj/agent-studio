#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

MEMORY_ANALYZER_PROMPT = """
# 任务描述
你是一个记忆引擎,你的任务是分析当前消息`current_messages`，结合历史消息`historical_messages`,对用户对话记忆进行多维度分析,实现记忆分类、变量提取和用户画像提取.

## 处理步骤
请严格按照以下三个步骤顺序执行:

### 步骤1：记忆分类`category`
请将用户提供的信息进行分类，结果可能属于多个类别；如果不属于任何类别，则结果为空列表 `[]`。
`user_profile` ：指与用户相关的具体信息，包括但不限于以下方面：
  - 性别、年龄、职业、学历、居住地等个人基本信息
  - 兴趣爱好与生活习惯
  - 资产信息与财务状况（如收入、房产、车辆、投资、负债等）
  - 社交关系与沟通方式（如朋友、同事、伴侣、社交习惯等）
  - 不属于以上类别但对用户画像有价值的其他信息

"""

MEMORY_ANALYZER_VARIABLE_PROMPT = """
### 步骤STEP：记忆变量提取
分析对话内容，根据预定义的变量名称和变量描述，完成目标变量值提取。
变量定义如下:
variables_description_template

"""

MEMORY_ANALYZER_USER_PROFILE_PROMPT = """
### 步骤STEP:
#### 提取规则
1.   提取**用户画像**基于如下多条描述，每条描述含有一个标识符和描述，描述内包含需要提供的用户画像所属的topic和对应的tag，以及详细描述信息：
     user_profile_topic_description_template
2.   注意不要提取以下信息：user_profile_forbidden_topic_template
#### 提取步骤
1.   提取一个或者多个用户画像。
2.   将提取出的用户画像归类到不同的画像维度中，每个类别可能生成多个用户画像。

"""

OUTPUT_PROMPT = """
## 输出格式
1.   最终的输出必须是**纯净的，可直接解析的JSON对象**，不要有任何额外的解释性文字。
2.   不要修改或新增输出结构。
3.   保留Markdown格式```json``` 代码块标记。
4.   根据你的分析，填充以下JSON结构，确保所有字段即使为空也用空字符串或空列表表示。
```json
{
  "category": []
  variables_template
  user_profile_topic_template
}
```
"""
