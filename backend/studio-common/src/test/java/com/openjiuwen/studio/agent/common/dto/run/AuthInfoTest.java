/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;

class AuthInfoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        AuthKeyInfo keyInfo = new AuthKeyInfo().setTargetName("t").setAuthKey("k");
        AuthInfo info = new AuthInfo()
                .setScope(AuthInfo.ScopeEnum.USER)
                .setDomain(AuthInfo.DomainEnum.HEADERS)
                .setAuthKeys(List.of(keyInfo));
        assertEquals(AuthInfo.ScopeEnum.USER, info.getScope());
        assertEquals(AuthInfo.DomainEnum.HEADERS, info.getDomain());
        assertEquals(1, info.getAuthKeys().size());
        assertEquals(keyInfo, info.getAuthKeys().get(0));
    }

    @Test
    void testScopeEnum_Values() {
        assertEquals("USER", AuthInfo.ScopeEnum.USER.toString());
        assertEquals("SERVICE", AuthInfo.ScopeEnum.SERVICE.toString());
    }

    @Test
    void testScopeEnum_FromValue() {
        assertEquals(AuthInfo.ScopeEnum.USER, AuthInfo.ScopeEnum.fromValue("USER"));
        assertEquals(AuthInfo.ScopeEnum.SERVICE, AuthInfo.ScopeEnum.fromValue("SERVICE"));
        assertNull(AuthInfo.ScopeEnum.fromValue("UNKNOWN"));
    }

    @Test
    void testDomainEnum_Values() {
        assertEquals("HEADERS", AuthInfo.DomainEnum.HEADERS.toString());
        assertEquals("QUERY", AuthInfo.DomainEnum.QUERY.toString());
    }

    @Test
    void testDomainEnum_FromValue() {
        assertEquals(AuthInfo.DomainEnum.HEADERS, AuthInfo.DomainEnum.fromValue("HEADERS"));
        assertEquals(AuthInfo.DomainEnum.QUERY, AuthInfo.DomainEnum.fromValue("QUERY"));
        assertNull(AuthInfo.DomainEnum.fromValue("UNKNOWN"));
    }

    @Test
    void testEquals_SameInstance() {
        AuthInfo info = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER);
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        AuthInfo a1 = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER).setDomain(AuthInfo.DomainEnum.HEADERS);
        AuthInfo a2 = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER).setDomain(AuthInfo.DomainEnum.HEADERS);
        assertEquals(a1, a2);
    }

    @Test
    void testEquals_Null() {
        AuthInfo info = new AuthInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        AuthInfo info = new AuthInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        AuthInfo a1 = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER).setDomain(AuthInfo.DomainEnum.HEADERS);
        AuthInfo a2 = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER).setDomain(AuthInfo.DomainEnum.HEADERS);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AuthInfo info = new AuthInfo().setScope(AuthInfo.ScopeEnum.USER);
        assertNotNull(info.toString());
    }
}
