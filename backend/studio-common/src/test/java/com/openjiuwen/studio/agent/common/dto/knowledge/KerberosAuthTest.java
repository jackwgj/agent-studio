/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KerberosAuthTest {

    @Test
    void testGettersAndSetters_AllFields() {
        KerberosAuth auth = new KerberosAuth();
        auth.setHostNames("host1");
        auth.setClusterIps("10.0.0.1");
        auth.setPort("8080");
        auth.setProtocol("https");
        auth.setUserKeytabFile("/path/keytab");
        auth.setKrb5File("/path/krb5");

        assertEquals("host1", auth.getHostNames());
        assertEquals("10.0.0.1", auth.getClusterIps());
        assertEquals("8080", auth.getPort());
        assertEquals("https", auth.getProtocol());
        assertEquals("/path/keytab", auth.getUserKeytabFile());
        assertEquals("/path/krb5", auth.getKrb5File());
    }

    @Test
    void testEquals_SameObject() {
        KerberosAuth auth = new KerberosAuth();
        auth.setPort("8080");
        assertEquals(auth, auth);
    }

    @Test
    void testEquals_EqualObject() {
        KerberosAuth a1 = new KerberosAuth();
        a1.setClusterIps("10.0.0.1");
        a1.setPort("8080");
        KerberosAuth a2 = new KerberosAuth();
        a2.setClusterIps("10.0.0.1");
        a2.setPort("8080");
        assertEquals(a1, a2);
    }

    @Test
    void testEquals_Null() {
        KerberosAuth auth = new KerberosAuth();
        assertNotEquals(null, auth);
    }

    @Test
    void testEquals_DifferentClass() {
        KerberosAuth auth = new KerberosAuth();
        assertNotEquals("string", auth);
    }

    @Test
    void testHashCode_Consistency() {
        KerberosAuth a1 = new KerberosAuth();
        a1.setPort("8080");
        KerberosAuth a2 = new KerberosAuth();
        a2.setPort("8080");
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        KerberosAuth auth = new KerberosAuth();
        auth.setPort("8080");
        assertNotNull(auth.toString());
    }

    @Test
    void testIsValid_AllRequiredFieldsPresent() {
        KerberosAuth auth = new KerberosAuth();
        auth.setClusterIps("10.0.0.1");
        auth.setPort("8080");
        auth.setProtocol("https");
        assertTrue(auth.isValid());
    }

    @Test
    void testIsValid_ClusterIpsNull() {
        KerberosAuth auth = new KerberosAuth();
        auth.setPort("8080");
        auth.setProtocol("https");
        assertThrows(NullPointerException.class, auth::isValid);
    }

    @Test
    void testIsValid_PortNull() {
        KerberosAuth auth = new KerberosAuth();
        auth.setClusterIps("10.0.0.1");
        auth.setProtocol("https");
        assertThrows(NullPointerException.class, auth::isValid);
    }

    @Test
    void testIsValid_ProtocolNull() {
        KerberosAuth auth = new KerberosAuth();
        auth.setClusterIps("10.0.0.1");
        auth.setPort("8080");
        assertThrows(NullPointerException.class, auth::isValid);
    }
}
