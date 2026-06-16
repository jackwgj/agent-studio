/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonServiceTest {

    private final CommonService commonService = new CommonService();

    @Test
    void testValidSearchName_ValidChinese() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("测试名称"));
    }

    @Test
    void testValidSearchName_ValidEnglish() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("testName"));
    }

    @Test
    void testValidSearchName_ValidWithNumbers() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("123test"));
    }

    @Test
    void testValidSearchName_ValidWithUnderscore() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("test_name"));
    }

    @Test
    void testValidSearchName_ValidWithHyphen() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("test-name"));
    }

    @Test
    void testValidSearchName_ValidWithParens() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("test(1)"));
    }

    @Test
    void testValidSearchName_BlankName() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber(""));
    }

    @Test
    void testValidSearchName_NullName() {
        assertDoesNotThrow(() -> commonService.validSearchNameStartWithChineseEnglishLettersNumber(null));
    }

    @Test
    void testValidSearchName_InvalidSpecialChars() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validSearchNameStartWithChineseEnglishLettersNumber("test@name"));
    }

    @Test
    void testValidSearchName_TooLong() {
        String longName = "a".repeat(65);
        assertThrows(AgentStudioException.class,
                () -> commonService.validSearchNameStartWithChineseEnglishLettersNumber(longName));
    }

    @Test
    void testValidName_ValidChinese() {
        assertDoesNotThrow(() -> commonService.validNameStartWithChineseEnglishLetters("测试名称"));
    }

    @Test
    void testValidName_ValidEnglish() {
        assertDoesNotThrow(() -> commonService.validNameStartWithChineseEnglishLetters("testName"));
    }

    @Test
    void testValidName_BlankName() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters(""));
    }

    @Test
    void testValidName_NullName() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters(null));
    }

    @Test
    void testValidName_StartsWithNumber() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters("1test"));
    }

    @Test
    void testValidName_StartsWithUnderscore() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters("_test"));
    }

    @Test
    void testValidName_StartsWithHyphen() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters("-test"));
    }

    @Test
    void testValidName_TooShort() {
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters("a"));
    }

    @Test
    void testValidName_TooLong() {
        String longName = "a" + "b".repeat(64);
        assertThrows(AgentStudioException.class,
                () -> commonService.validNameStartWithChineseEnglishLetters(longName));
    }

    @Test
    void testValidName_ExactlyMaxLength() {
        String name = "a" + "b".repeat(63);
        assertDoesNotThrow(() -> commonService.validNameStartWithChineseEnglishLetters(name));
    }

    @Test
    void testValidName_MinLength() {
        assertDoesNotThrow(() -> commonService.validNameStartWithChineseEnglishLetters("ab"));
    }
}
