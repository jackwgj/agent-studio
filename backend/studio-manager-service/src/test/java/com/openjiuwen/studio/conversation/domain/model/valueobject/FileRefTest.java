package com.openjiuwen.studio.conversation.domain.model.valueobject;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FileRefTest {

    @Test
    void testDurableArtifactMetadata_RoundTripsWithObjectKey() {
        FileRef expected = new FileRef("conversation-artifacts/p/w/u/c/e/a-report.pdf", "report.pdf",
            128L, "application/pdf", "0".repeat(64), "exec-1");

        String json = JSON.toJSONString(expected);
        FileRef actual = JSON.parseObject(json, FileRef.class);

        assertFalse(json.contains("\"key\""));
        assertEquals(expected, actual);
    }

    @Test
    void testLegacyKey_RemainsReadable() {
        FileRef ref = JSON.parseObject("{\"key\":\"legacy/object\",\"fileName\":\"old.txt\"}", FileRef.class);

        assertEquals("legacy/object", ref.getObjectKey());
        assertEquals("old.txt", ref.getFileName());
    }
}
