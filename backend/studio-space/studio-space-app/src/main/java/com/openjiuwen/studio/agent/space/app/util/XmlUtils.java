/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import org.xml.sax.XMLReader;

public class XmlUtils {
    public static void addXxeFeature(XMLReader reader) {
        if (null != reader) {
            try {
                // XXE 防御
                reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            } catch (final Exception e) {
                throw new RuntimeException("Unable to create XMLReader", e);
            }
        }
    }
}
