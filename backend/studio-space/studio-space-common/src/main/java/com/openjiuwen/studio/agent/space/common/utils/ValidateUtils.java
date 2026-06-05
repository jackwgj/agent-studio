package com.openjiuwen.studio.agent.space.common.utils;

import java.util.List;

public class ValidateUtils {

    public static boolean isEmptyString(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmptyString(String str) {
        return !isEmptyString(str);
    }

    public static <T> boolean isEmptyCollection(List<T> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    public static <T> boolean isNotEmptyCollection(List<T> collection) {
        return !isEmptyCollection(collection);
    }
}
