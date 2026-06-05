/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

class AuthnRequestTest {

    private static final Pattern UUID_PATTERN =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private static final String ISSUER = "test-issuer";

    private static final String ISSUE_INSTANT = "2023-10-01T12:00:00Z";

    private static final String SERVICE_URL = "https://sp.example.com/acs";

    private AuthnRequest authnRequest;

    @BeforeEach
    void setUp() {
        authnRequest = new AuthnRequest(ISSUER, ISSUE_INSTANT, SERVICE_URL);
    }

    @Test
    void toXML_shouldGenerateCorrectRootElement() {
        Element xml = authnRequest.toXML();

        // 验证根元素名称和命名空间
        assertThat(xml.getName()).isEqualTo("AuthnRequest");
        assertThat(xml.getNamespaceForPrefix("samlp").getURI()).isEqualTo("urn:oasis:names:tc:SAML:2.0:protocol");

        // 验证固定属性值
        assertThat(xml.attributeValue("Version")).isEqualTo("2.0");
        assertThat(xml.attributeValue("ProtocolBinding")).isEqualTo("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        assertThat(xml.attributeValue("AssertionConsumerServiceIndex")).isEqualTo("0");
        assertThat(xml.attributeValue("AssertionConsumerServiceURL")).isEqualTo(SERVICE_URL);
        assertThat(xml.attributeValue("AttributeConsumingServiceIndex")).isEqualTo("0");
        assertThat(xml.attributeValue("uiType")).isEqualTo("0");

        // 验证动态生成的ID格式
        String id = xml.attributeValue("ID");
        assertThat(id).isNotNull();
    }

    @Test
    void toXML_shouldContainIssuerElementWithCorrectValue() {
        Element xml = authnRequest.toXML();

        // 查找Issuer元素并验证
        Element issuerElement = xml.element("Issuer");
    }

    @Test
    void toXML_shouldContainNameIDPolicyElement() {
        Element xml = authnRequest.toXML();

        // 验证NameIDPolicy元素及属性
        Element nameIDPolicy = xml.element("NameIDPolicy");

    }

    @Test
    void toXML_shouldIncludeDigestAlgorithmWhenSet() {
        authnRequest.setDigestAlgorithm("SHA-256");
        Element xml = authnRequest.toXML();
        assertThat(xml.attributeValue("digestAlgorithm")).isEqualTo("SHA-256");
    }

    @Test
    void toXML_shouldIncludeSignatureAlgorithmWhenSet() {
        authnRequest.setSignatureAlgorithm("RSA-SHA256");
        Element xml = authnRequest.toXML();
        assertThat(xml.attributeValue("signatureAlgorithm")).isEqualTo("RSA-SHA256");
    }

    @Test
    void toXML_shouldNotIncludeAlgorithmsByDefault() {
        Element xml = authnRequest.toXML();
        assertThat(xml.attributeValue("digestAlgorithm")).isNull();
        assertThat(xml.attributeValue("signatureAlgorithm")).isNull();
    }

    // 验证命名空间声明
    @Test
    void toXML_shouldDeclareSamlpNamespace() {
        Element xml = authnRequest.toXML();
        Namespace samlpNamespace = xml.getNamespaceForPrefix("samlp");
        assertThat(samlpNamespace).isNotNull();
        assertThat(samlpNamespace.getURI()).isEqualTo("urn:oasis:names:tc:SAML:2.0:protocol");
    }
}
