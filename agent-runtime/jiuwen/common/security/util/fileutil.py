#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""File-related util functions"""

import os


def read_file(file_path):
    """
    Reads the file content.
    If the file is readable, the file content is returned.
    Otherwise, an error message is returned.
    """
    try:
        if os.access(file_path, os.R_OK):
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    text = f.read()
                    return True, text
            except Exception:
                return False, "read file error"
        else:
            return False, "cannot read the file"
    except Exception:
        return False, "Unknown exception occurred while read file"
