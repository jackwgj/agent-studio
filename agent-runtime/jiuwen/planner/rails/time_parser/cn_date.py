# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.

from datetime import datetime, timedelta


class LunarSolarDate:
    def __init__(self):
        self.tiangan = "甲乙丙丁戊己庚辛壬癸"
        self.dizhi = "子丑寅卯辰巳午未申酉戌亥"
        self.zodiac = "鼠牛虎兔龙蛇马羊猴鸡狗猪"
        self.cn_num = "零一二三四五六七八九十"
        self.cn_new_year = [
            "20000205",
            "20010124",
            "20020212",
            "20030201",
            "20040122",
            "20050209",
            "20060129",
            "20070218",
            "20080207",
            "20090126",
            "20100214",
            "20110203",
            "20120123",
            "20130210",
            "20140131",
            "20150219",
            "20160208",
            "20170128",
            "20180216",
            "20190205",
            "20200125",
            "20210212",
            "20220201",
            "20230122",
            "20240210",
            "20250129",
            "20260217",
            "20270206",
            "20280126",
            "20290213",
            "20300203",
            "20310123",
            "20320211",
            "20330131",
            "20340219",
            "20350208",
            "20360128",
            "20370215",
            "20380204",
            "20390124",
            "20400212",
            "20410201",
            "20420122",
            "20430210",
            "20440130",
            "20450217",
            "20460206",
            "20470126",
            "20480214",
            "20490202",
            "20500123",
            "20510211",
            "20520201",
            "20530219",
            "20540208",
            "20550128",
            "20560215",
            "20570204",
            "20580124",
            "20590212",
            "20600202",
            "20610121",
            "20620209",
            "20630129",
            "20640217",
            "20650205",
            "20660126",
            "20670214",
            "20680203",
            "20690123",
            "20700211",
            "20710131",
            "20720219",
            "20730207",
            "20740127",
            "20750215",
            "20760205",
            "20770124",
            "20780212",
            "20790202",
            "20800122",
            "20810209",
            "20820129",
            "20830217",
            "20840206",
            "20850126",
            "20860214",
            "20870203",
            "20880124",
            "20890210",
            "20900130",
            "20910218",
            "20920207",
            "20930127",
            "20940215",
            "20950205",
            "20960125",
            "20970212",
            "20980201",
            "20990121",
            "21000209",
        ]

        self.cn_year_code = [
            51552,
            55636,
            54432,
            55888,
            30034,
            22176,
            43959,
            9680,
            37584,
            51893,
            43344,
            46240,
            47780,
            44368,
            21977,
            19360,
            42416,
            86390,
            21168,
            43312,
            31060,
            27296,
            44368,
            23378,
            19296,
            42726,
            42208,
            53856,
            60005,
            54576,
            23200,
            30371,
            38608,
            19195,
            19152,
            42192,
            118966,
            53840,
            54560,
            56645,
            46496,
            22224,
            21938,
            18864,
            42359,
            42160,
            43600,
            111189,
            27936,
            44448,
            84835,
            37744,
            18936,
            18800,
            25776,
            92326,
            59984,
            27296,
            108228,
            43744,
            37600,
            53987,
            51552,
            54615,
            54432,
            55888,
            23893,
            22176,
            42704,
            21972,
            21200,
            43448,
            43344,
            46240,
            46758,
            44368,
            21920,
            43940,
            42416,
            21168,
            45683,
            26928,
            29495,
            27296,
            44368,
            84821,
            19296,
            42352,
            21732,
            53600,
            59752,
            54560,
            55968,
            92838,
            22224,
            19168,
            43476,
            41680,
            53584,
            62034,
            54560,
        ]

    @staticmethod
    def _decode(code):
        days = list()
        for i in range(5, 17):
            if (code >> (i - 1)) & 1:
                days.insert(0, 30)
            else:
                days.insert(0, 29)
        if code & 0xF:
            if code >> 16:
                days.insert((code & 0xF), 30)
            else:
                days.insert((code & 0xF), 29)
        return days

    def _to_solar_date(self, lunar_y, lunar_m, lunar_d, leap_m=False):
        if not self._validate(lunar_y, lunar_m, lunar_d, leap_m):
            raise ValueError("日期不支持")

        new_year = datetime.strptime(self.cn_new_year[lunar_y - 2000], "%Y%m%d")
        time_delta = timedelta(
            days=self._lunar_days_passed(lunar_y, lunar_m, lunar_d, leap_m)
        )
        return new_year + time_delta

    def _lunar_days_passed(self, lunar_y, lunar_m, lunar_d, leap_m):
        code = self.cn_year_code[lunar_y - 2000]

        days = LunarSolarDate._decode(code)
        leap = code & 0xF

        if (leap == 0) or (lunar_m < leap):
            passed_d = sum(days[: lunar_m - 1])
        elif (not leap_m) and (lunar_m == leap):
            passed_d = sum(days[: lunar_m - 1])
        else:
            passed_d = sum(days[:lunar_m])

        return passed_d + lunar_d - 1

    def _validate(self, y, m, d, leap):
        if not (2000 <= y <= 2100 and 1 <= m <= 12 and 1 <= d <= 30):
            return False

        code = self.cn_year_code[y - 2000]

        if leap:
            if (code & 0xF) != m:
                return False
            if d == 30:
                return (code >> 16) == 1
            return True
        if d <= 29:
            return True
        return ((code >> (12 - m) + 4) & 1) == 1
