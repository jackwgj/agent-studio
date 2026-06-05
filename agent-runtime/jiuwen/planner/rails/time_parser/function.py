# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.


def absence(regular_expression):
    """absense method"""
    return "".join([regular_expression, r"?"])


def bracket(regular_expression):
    """bracket method"""
    return "".join([r"(", regular_expression, r")"])


def bracket_absence(regular_expression):
    """bracket_absence method"""
    return "".join([r"(", regular_expression, r")?"])
