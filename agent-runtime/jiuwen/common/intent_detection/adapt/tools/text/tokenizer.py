#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

import re
from typing import List

_REGEX_LETTER_NUMBER = r"[a-zA-Z0-9]"
_REGEX_NOT_LETTER_NUMBER = r"[^a-zA-Z0-9]"
_REGEX_SEPARATOR = r"[\\?!()\";/\\|`]"
_REGEX_CLITICS = r"'|:|-|'S|'D|'M|'LL|'RE|'VE|N'T|'s|'d|'m|'ll|'re|'ve|n't"

_RE_TAB = re.compile(r"\t")
_RE_SEP = re.compile(rf"({_REGEX_SEPARATOR})")
_RE_COMMA_PRE = re.compile(r"([^0-9]),")
_RE_COMMA_POST = re.compile(r",([^0-9])")
_RE_APOS_START = re.compile(r"^(')")
_RE_APOS_BEFORE = re.compile(rf"({_REGEX_NOT_LETTER_NUMBER})\'")
_RE_CLITICS_END = re.compile(rf"({_REGEX_CLITICS})$")
_RE_CLITICS_MID = re.compile(rf"({_REGEX_CLITICS})({_REGEX_NOT_LETTER_NUMBER})")

_ABBREVIATIONS = {
    "Co.",
    "Corp.",
    "vs.",
    "e.g.",
    "etc.",
    "ex.",
    "cf.",
    "eg.",
    "Jan.",
    "Feb.",
    "Mar.",
    "Apr.",
    "Jun.",
    "Jul.",
    "Aug.",
    "Sept.",
    "Oct.",
    "Nov.",
    "Dec.",
    "jan.",
    "feb.",
    "mar.",
    "apr.",
    "jun.",
    "jul.",
    "aug.",
    "sept.",
    "oct.",
    "nov.",
    "dec.",
    "ed.",
    "eds.",
    "repr.",
    "trans.",
    "vol.",
    "vols.",
    "rev.",
    "est.",
    "b.",
    "m.",
    "bur.",
    "d.",
    "r.",
    "M.",
    "Dept.",
    "MM.",
    "U.",
    "Mr.",
    "Jr.",
    "Ms.",
    "Mme.",
    "Mrs.",
    "Dr.",
    "Ph.D.",
}

_RE_LETTER_DOT = re.compile(rf".*{_REGEX_LETTER_NUMBER}\.")
_RE_SINGLE_LETTER_DOT = re.compile(
    r"^([A-Za-z]\.([A-Za-z]\.)+|[A-Z][bcdfghj-nptvxz]+\.)$"
)


class EnglishTokenizer:
    def __init__(self):
        pass

    @staticmethod
    def tokenize(string: str) -> List[str]:
        """
        将输入字符串解析为 token 列表。

        Notes:
            - 会将 'don't' 拆为 ['do', "n't"] —— 与原行为一致；
            - 对非缩写词结尾的 '.'（如 'word.'）拆为 ['word', '.']；
            - 缩写词（如 'Mr.'）保持原样。
        """
        s = string

        # 替换制表符为空格
        s = _RE_TAB.sub(" ", s)

        # 在分隔符前后加空格（如 ? ! ( ) 等）
        s = _RE_SEP.sub(r" \1 ", s)

        # 处理逗号：数字后/前的逗号不加空格，其余加（如 a, → a ,；但 1,000 不变）
        s = _RE_COMMA_PRE.sub(r"\1 , ", s)
        s = _RE_COMMA_POST.sub(r" , \1", s)

        # 处理开头单引号：'hello → ' hello
        s = _RE_APOS_START.sub(r"\1 ", s)

        # 处理非字母数字后的单引号：x' → x '
        s = _RE_APOS_BEFORE.sub(r"\1 ' ", s)

        # 处理词尾附着词（clitics）：don't → don 't；但保留缩写如 it's → it 's
        s = _RE_CLITICS_END.sub(r" \1", s)
        s = _RE_CLITICS_MID.sub(r" \1 \2", s)

        # 拆分为初步 token
        words = s.strip().split()

        token_list = []
        for word in words:
            # 判断是否为带点单词（如 word.）
            is_letter_dot = _RE_LETTER_DOT.match(word) is not None
            is_abbrev_like = _RE_SINGLE_LETTER_DOT.match(word) is not None
            is_known_abbrev = word in _ABBREVIATIONS

            # 若是带点单词，但非缩写、也非特殊缩写模式 → 拆点
            if is_letter_dot and not is_known_abbrev and not is_abbrev_like:
                dot_pos = word.find(".")
                token_list.append(word[:dot_pos])
                token_list.append(word[dot_pos])  # 即 '.'
            else:
                token_list.append(word)

        return token_list


def tokenize_string(text: str) -> List[str]:
    """辅助函数：直接返回分词结果，方便测试。"""
    return EnglishTokenizer().tokenize(text)
