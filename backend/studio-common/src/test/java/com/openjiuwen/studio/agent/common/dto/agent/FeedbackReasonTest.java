/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FeedbackReasonTest {

    @Test
    void testDefaultTags() {
        FeedbackReason reason = new FeedbackReason();
        assertNotNull(reason.getTags());
        assertTrue(reason.getTags().isEmpty());
    }

    @Test
    void testSetGetTags() {
        FeedbackReason reason = new FeedbackReason();
        List<String> tags = List.of("tag1", "tag2");
        FeedbackReason result = reason.setTags(tags);
        assertSame(reason, result);
        assertEquals(tags, reason.getTags());
    }

    @Test
    void testSetGetContent() {
        FeedbackReason reason = new FeedbackReason();
        assertNull(reason.getContent());
        FeedbackReason result = reason.setContent("feedback content");
        assertSame(reason, result);
        assertEquals("feedback content", reason.getContent());
    }

    @Test
    void testChainedSetters() {
        FeedbackReason reason = new FeedbackReason()
                .setTags(List.of("good", "helpful"))
                .setContent("great response");
        assertEquals(2, reason.getTags().size());
        assertEquals("great response", reason.getContent());
    }

    @Test
    void testEquals_SameInstance() {
        FeedbackReason reason = new FeedbackReason();
        assertEquals(reason, reason);
    }

    @Test
    void testEquals_EqualObjects() {
        FeedbackReason r1 = new FeedbackReason().setTags(List.of("tag1")).setContent("content");
        FeedbackReason r2 = new FeedbackReason().setTags(List.of("tag1")).setContent("content");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        FeedbackReason r1 = new FeedbackReason().setContent("content1");
        FeedbackReason r2 = new FeedbackReason().setContent("content2");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        FeedbackReason reason = new FeedbackReason();
        assertNotEquals(null, reason);
        assertNotEquals("string", reason);
    }

    @Test
    void testHashCode_Consistency() {
        FeedbackReason r1 = new FeedbackReason().setTags(List.of("tag1")).setContent("content");
        FeedbackReason r2 = new FeedbackReason().setTags(List.of("tag1")).setContent("content");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        FeedbackReason reason = new FeedbackReason().setTags(List.of("tag1")).setContent("content");
        String str = reason.toString();
        assertNotNull(str);
        assertTrue(str.contains("FeedbackReason"));
        assertTrue(str.contains("tag1"));
        assertTrue(str.contains("content"));
    }

    @Test
    void testToString_NullFields() {
        FeedbackReason reason = new FeedbackReason();
        reason.setTags(null);
        String str = reason.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
