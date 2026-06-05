/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class UriUtils {

    /**
     * Normalize and validate URI string.
     *
     * @param uri the URI string to normalize
     * @return normalized URI string
     * @throws Exception if the URI is invalid
     */
    public static String normalizeURI(String uri) throws Exception {
        if (uri == null || uri.isEmpty()) {
            return uri;
        }
        try {
            URI parsed = new URI(uri);
            return parsed.normalize().toString();
        } catch (URISyntaxException e) {
            throw new Exception("Invalid URI: " + uri, e);
        }
    }
}