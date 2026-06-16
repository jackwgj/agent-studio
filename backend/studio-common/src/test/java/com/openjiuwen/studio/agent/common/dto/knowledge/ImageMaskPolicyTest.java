/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageMaskPolicyTest {

    @Test
    void testValues() {
        ImageMaskPolicy[] values = ImageMaskPolicy.values();
        assertEquals(3, values.length);
    }

    @Test
    void testValueOf() {
        assertEquals(ImageMaskPolicy.RETAIN_IMAGE_ID, ImageMaskPolicy.valueOf("RETAIN_IMAGE_ID"));
        assertEquals(ImageMaskPolicy.RETAIN_PLACEHOLDER, ImageMaskPolicy.valueOf("RETAIN_PLACEHOLDER"));
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.valueOf("REMOVE_IMAGE"));
    }

    @Test
    void testFromString_ValidRetainImageId() {
        assertEquals(ImageMaskPolicy.RETAIN_IMAGE_ID, ImageMaskPolicy.fromString("RETAIN_IMAGE_ID"));
    }

    @Test
    void testFromString_ValidRetainPlaceholder() {
        assertEquals(ImageMaskPolicy.RETAIN_PLACEHOLDER, ImageMaskPolicy.fromString("RETAIN_PLACEHOLDER"));
    }

    @Test
    void testFromString_ValidRemoveImage() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString("REMOVE_IMAGE"));
    }

    @Test
    void testFromString_Null() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString(null));
    }

    @Test
    void testFromString_Empty() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString(""));
    }

    @Test
    void testFromString_Blank() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString("   "));
    }

    @Test
    void testFromString_Invalid() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString("INVALID"));
    }

    @Test
    void testFromString_CaseSensitive() {
        assertEquals(ImageMaskPolicy.REMOVE_IMAGE, ImageMaskPolicy.fromString("retain_image_id"));
    }
}
