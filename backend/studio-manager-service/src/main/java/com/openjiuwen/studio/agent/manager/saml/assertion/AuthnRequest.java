/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.assertion;

import com.openjiuwen.studio.agent.manager.saml.response.SecureIdGenerator;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class AuthnRequest {

    public static final Namespace SAMLP =
        DocumentHelper.createNamespace("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");

    public static final Namespace SAML =
        DocumentHelper.createNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");

    private final String issuer;

    private final String issueInstant;

    private final String serviceUrl;

    private String digestAlgorithm;

    private String signatureAlgorithm;

    public AuthnRequest(String issuer, String issueInstant, String serviceUrl) {
        this.issuer = issuer;
        this.issueInstant = issueInstant;
        this.serviceUrl = serviceUrl;
    }

    /**
     * @return
     */
    public Element toXML() {

        Element request = DocumentHelper.createElement("AuthnRequest");
        request.addAttribute("xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        request.addAttribute("ID", new SecureIdGenerator().generateSecureId());
        request.addAttribute("Version", "2.0");
        request.addAttribute("IssueInstant", this.issueInstant);
        request.addAttribute("ProtocolBinding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        request.addAttribute("AssertionConsumerServiceIndex", "0");
        request.addAttribute("AssertionConsumerServiceURL", this.serviceUrl);
        request.addAttribute("AttributeConsumingServiceIndex", "0");
        request.addAttribute("source", "");
        request.addAttribute("uiType", "0");

        if (digestAlgorithm != null) {
            request.addAttribute("digestAlgorithm", digestAlgorithm);
        }
        if (signatureAlgorithm != null) {
            request.addAttribute("signatureAlgorithm", signatureAlgorithm);
        }
        Element issuerElement = DocumentHelper.createElement("saml:Issuer");
        issuerElement.addAttribute("xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        issuerElement.setText(this.issuer);
        issuerElement.add(SAML);
        Element nameIDPolicy = DocumentHelper.createElement("samlp:NameIDPolicy");
        nameIDPolicy.addAttribute("AllowCreate", "False");
        nameIDPolicy.add(SAMLP);
        request.add(issuerElement);
        request.add(nameIDPolicy);
        request.add(SAMLP);
        return request;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

}
