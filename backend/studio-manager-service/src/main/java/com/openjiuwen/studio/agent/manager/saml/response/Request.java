/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.response;

import com.openjiuwen.studio.agent.manager.saml.SAMLException;
import com.openjiuwen.studio.agent.manager.saml.SAMLUtil;
import com.openjiuwen.studio.agent.manager.saml.assertion.AuthnRequest;

import org.dom4j.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.security.KeyStore.PrivateKeyEntry;

public class Request {
    private final AuthnRequest request;

    private final PrivateKeyEntry privateKey;

    public Request(AuthnRequest request, PrivateKeyEntry privateKey) {
        this.request = request;
        this.privateKey = privateKey;
    }

    /**
     * @return
     * @throws SAMLException
     */
    public Element toXML() throws SAMLException {
        Element req = request.toXML();

        if (privateKey != null) {
            Document doc = SAMLUtil.toDomcument(req);
            org.w3c.dom.Element element = doc.getDocumentElement();
            NodeList nodeList = doc.getElementsByTagName("saml:Issuer");
            org.w3c.dom.Node insertBefore = nodeList.item(nodeList.getLength() - 1);
            SAMLUtil.insertSignature(element, privateKey, insertBefore, request.getDigestAlgorithm(),
                request.getSignatureAlgorithm());
            req = SAMLUtil.toDom4jElement(doc);
            req.detach();
        }
        return req;
    }

}
