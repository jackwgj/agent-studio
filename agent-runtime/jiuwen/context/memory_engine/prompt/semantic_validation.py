#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.

SEMANTIC_VALIDATION_PROMPT = """
# 语义一致性判定

## 规则
- **CORRECT**：待判断内容与标准内容语义一致，或为标准的具体实例（概括兼容）
- **WRONG**：关键信息矛盾，或待判断**缩小/否定**了标准的范围

## 关键原则
**标准内容越抽象/宽泛，待判断越具体 → CORRECT**  
**标准内容具体，待判断不同/缺失 → WRONG**

## 敏感属性（仅当标准明确限定时需匹配）
- 标准含具体时间 → 待判断不能矛盾
- 标准含具体地点 → 待判断不能矛盾
- 标准未限定 → 待判断补充细节视为CORRECT

## 示例
| 标准 | 待判断 | 结果 |
|------|--------|------|
| 用户所在的城市是北京 | 用户现在住在北京 | CORRECT |
| 用户昨天去了上海 | 用户去过上海 | WRONG（标准限定昨天） |
| 用户的职业相关的内容 | 用户在北京工作 | CORRECT（概括） |
| 用户的爱好是羽毛球 | 用户昨天打羽毛球 | WRONG（爱好≠单次行为） |
| 关于会议时间的记忆 | 会议在明天上午 | CORRECT（标准未限定，补充细节） |

## 输入
标准：{old_mem}
待判断：{obtained_mem}

## 输出
仅输出 {{"result": "CORRECT"}} 或 {{"result": "WRONG"}}
"""
