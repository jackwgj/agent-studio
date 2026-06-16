/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FilePathSecurityUtilsTest {

    @Test
    public void testIsSecurityFileName_Null() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName(null));
    }

    @Test
    public void testIsSecurityFileName_Empty() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName(""));
    }

    @Test
    public void testIsSecurityFileName_Blank() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("   "));
    }

    @Test
    public void testIsSecurityFileName_NormalFileName() {
        assertTrue(FilePathSecurityUtils.isSecurityFileName("test.txt"));
    }

    @Test
    public void testIsSecurityFileName_RelativePath() {
        assertTrue(FilePathSecurityUtils.isSecurityFileName("dir/test.txt"));
    }

    @Test
    public void testIsSecurityFileName_PathTraversal() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("../etc/passwd"));
    }

    @Test
    public void testIsSecurityFileName_PathTraversalMiddle() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("dir/../etc/passwd"));
    }

    @Test
    public void testIsSecurityFileName_AbsolutePathUnix() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("/etc/passwd"));
    }

    @Test
    public void testIsSecurityFileName_AbsolutePathWindows() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("C:\\Windows\\System32"));
    }

    @Test
    public void testIsSecurityFileName_NullByte() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName("test\0.txt"));
    }

    @Test
    public void testIsSecurityFileName_SimpleFileName() {
        assertTrue(FilePathSecurityUtils.isSecurityFileName("file.txt"));
    }

    @Test
    public void testIsSecurityFileName_FileNameWithSpaces() {
        assertTrue(FilePathSecurityUtils.isSecurityFileName("my file.txt"));
    }

    @Test
    public void testIsSecurityFileName_FileNameWithDots() {
        assertTrue(FilePathSecurityUtils.isSecurityFileName("my.file.name.txt"));
    }

    @Test
    public void testIsSecurityFileName_DoubleDotOnly() {
        assertFalse(FilePathSecurityUtils.isSecurityFileName(".."));
    }
}
