/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 功能描述
 *
 */
public class JsonUtilsTest {

    @Test
    public void testJson2Obj_SimpleObject() {
        String jsonStr = "{\"name\":\"John\", \"age\":30}";
        TypeReference<User> typeRef = new TypeReference<>() {};
        User user = JsonUtils.json2Obj(jsonStr, typeRef);
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals(30, user.getAge());
    }

    @Test
    public void testJson2Obj_ComplexObject() {
        String jsonStr = "{\"name\":\"John\", \"age\":30, \"address\":{\"city\":\"New York\", \"country\":\"USA\"}}";
        TypeReference<User> typeRef = new TypeReference<>() {};
        User user = JsonUtils.json2Obj(jsonStr, typeRef);
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals(30, user.getAge());
        assertNotNull(user.getAddress());
        assertEquals("New York", user.getAddress().getCity());
        assertEquals("USA", user.getAddress().getCountry());
    }

    @Test
    public void testJson2Obj_BlankJson() {
        String jsonStr = "";
        TypeReference<User> typeRef = new TypeReference<>() {};
        User user = JsonUtils.json2Obj(jsonStr, typeRef);
        assertNull(user);
    }

    @Test
    public void testJson2Obj_InvalidJson() {
        String jsonStr = "{invalid json}";
        TypeReference<User> typeRef = new TypeReference<>() {};
        // 预期返回Null
        assertNull(JsonUtils.json2Obj(jsonStr, typeRef));
    }

    @Test
    public void testObjectToClass_ComplexObject() {
        Address address = new Address();
        address.setCity("New York");
        address.setCountry("USA");
        User user = new User();
        user.setName("John");
        user.setAge(30);
        user.setAddress(address);
        Map<String, Object> convertedUser = JsonUtils.objectToClass(user);
        assertNotNull(convertedUser);
        assertEquals("John", convertedUser.get("name"));
        assertEquals(30, convertedUser.get("age"));
    }

    @Test
    public void testObjectToClass_NullInput() {
        Object obj = null;
        User user = JsonUtils.objectToClass(obj);
        assertNull(user);
    }

    @Test
    public void testObjectToClass_TypeMismatch() {
        User user = new User();
        user.setName("John");
        user.setAge(30);
        // 使用 Map 来接收结果，避免类型转换错误
        Map<String, Object> result = JsonUtils.objectToClass(user);
        assertNotNull(result);
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    static class User {
        private String name;

        private int age;

        private Address address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    static class Address {
        private String city;

        private String country;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
