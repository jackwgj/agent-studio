/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyUtilsTest {

    public static class SourceBean {
        private String label;
        private int score;

        public SourceBean() {}

        public SourceBean(String label, int score) {
            this.label = label;
            this.score = score;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
    }

    public static class TargetBean {
        private String label;
        private int score;

        public TargetBean() {}

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
    }

    @Test
    void testCopyProperties() {
        SourceBean source = new SourceBean("alpha", 100);
        TargetBean target = new TargetBean();
        Object result = CopyUtils.copyProperties(source, target);
        assertNotNull(result);
        assertEquals("alpha", target.getLabel());
        assertEquals(100, target.getScore());
    }

    @Test
    void testCopyPropertiesWithoutCommonAttrsTrans() {
        SourceBean source = new SourceBean("beta", 200);
        TargetBean target = new TargetBean();
        CopyUtils.copyProperties(source, target, false);
        assertEquals("beta", target.getLabel());
        assertEquals(200, target.getScore());
    }

    @Test
    void testCopyList() {
        List<SourceBean> sourceList = Arrays.asList(
                new SourceBean("item1", 10),
                new SourceBean("item2", 20)
        );
        List<TargetBean> result = CopyUtils.copyList(sourceList, TargetBean.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("item1", result.get(0).getLabel());
        assertEquals("item2", result.get(1).getLabel());
    }

    @Test
    void testCopyList_NullInput() {
        List<TargetBean> result = CopyUtils.copyList(null, TargetBean.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyList_EmptyInput() {
        List<TargetBean> result = CopyUtils.copyList(new ArrayList<>(), TargetBean.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyListWithCommonAttrsTrans() {
        List<SourceBean> sourceList = Arrays.asList(new SourceBean("gamma", 300));
        List<TargetBean> result = CopyUtils.copyList(sourceList, TargetBean.class, false);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("gamma", result.get(0).getLabel());
    }

    @Test
    void testCopyListWithCommonAttrsTrans_NullInput() {
        List<TargetBean> result = CopyUtils.copyList(null, TargetBean.class, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyListWithCommonAttrsTrans_EmptyInput() {
        List<TargetBean> result = CopyUtils.copyList(new ArrayList<>(), TargetBean.class, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
