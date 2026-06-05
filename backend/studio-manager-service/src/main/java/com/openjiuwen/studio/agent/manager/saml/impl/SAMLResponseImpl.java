/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.saml.impl;

import com.openjiuwen.studio.agent.manager.saml.SAMLException;
import com.openjiuwen.studio.agent.manager.saml.SAMLResponse;
import com.openjiuwen.studio.agent.manager.saml.SAMLUtil;
import com.openjiuwen.studio.agent.manager.saml.UserInfoBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@SuppressWarnings("unchecked")
public class SAMLResponseImpl implements SAMLResponse {
    private final String issuer;

    private final String notBefore;

    private final String notOnOrAfter;

    private final Node signature;

    private final String nameId;

    private final String cn;

    private final String sn;

    private final String givenName;

    private final String displayName;

    private final String employeeNumber;

    private final String employeeType;

    private final String uid;

    private final String uuid;

    private final String telephoneNumber;

    private final String mail;

    private final String departmentName;

    private final String source;

    private final String registerPhone;

    private final UserInfoBean uiBean = new UserInfoBean(UserInfoBean.INTERNET_USER);

    public SAMLResponseImpl(String response) throws SAMLException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8))) {
            SAMLUtil.setDocumentBuilderFeature(dbf);

            doc = dbf.newDocumentBuilder().parse(bais);
            doc.getDocumentElement().setIdAttribute("ID", true);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new SAMLException("Invalid SAML response", e);
        }
        String issuer = getIssuer(doc);
        if (issuer != null) {
            this.issuer = issuer;
        } else {
            throw new SAMLException("Invalid SAML response: miss <Issuer>");
        }
        Element condition = getCondition(doc);
        if (condition != null) {
            this.notBefore = condition.getAttribute("NotBefore");
            this.notOnOrAfter = condition.getAttribute("NotOnOrAfter");
        } else {
            throw new SAMLException("Invalid SAML response: miss <Conditions>");
        }
        Node signature = getSignature(doc);
        if (signature != null) {
            this.signature = signature;
        } else {
            throw new SAMLException("Invalid SAML response: miss <Signature>");
        }
        String nameId = getNameId(doc);
        if (nameId != null) {
            this.nameId = nameId;
        } else {
            throw new SAMLException("Invalid SAML response: miss <NameId>");
        }
        uiBean.setNameId(nameId);
        this.cn = getElementValue(doc, "cn");
        uiBean.setCn(cn);
        uiBean.setProperties(UserInfoBean.KEY_CN, cn);
        this.sn = getElementValue(doc, "sn");
        uiBean.setSn(sn);
        uiBean.setProperties(UserInfoBean.KEY_SN, sn);
        this.givenName = getElementValue(doc, "givenName");
        uiBean.setGivenName(givenName);
        uiBean.setProperties(UserInfoBean.KEY_GIVENNAME, givenName);
        this.displayName = getElementValue(doc, "displayName");
        this.departmentName = getElementValue(doc, "departmentName");
        this.uid = getElementValue(doc, "uid");
        uiBean.setUid(uid);
        uiBean.setProperties(UserInfoBean.KEY_UID, uid);
        this.uuid = getElementValue(doc, "uuid");
        uiBean.setProperties("uuid", uuid);
        this.telephoneNumber = getElementValue(doc, "telephoneNumber");
        uiBean.setProperties(UserInfoBean.KEY_TELEPHONENUMBER, telephoneNumber);
        this.mail = getElementValue(doc, "mail");
        uiBean.setEmail(mail);
        uiBean.setProperties(UserInfoBean.KEY_MAIL, mail);
        this.employeeNumber = getElementValue(doc, "employeeNumber");
        uiBean.setEmployeeNumber(employeeNumber);
        uiBean.setProperties(UserInfoBean.KEY_EMPLOYEENUMBER, employeeNumber);
        this.employeeType = getElementValue(doc, "employeeType");
        uiBean.setEmployeeType(employeeType);
        uiBean.setProperties(UserInfoBean.KEY_EMPLOYEETYPE, employeeType);
        this.source = getElementValue(doc, "source");
        this.registerPhone = getElementValue(doc, "registerphone");
        uiBean.setProperties("registerphone", registerPhone);
    }

    public static String getElementValue(Document doc, String elementName) {
        NodeList list = doc.getChildNodes();
        Node root = list.item(0);
        HashMap<String, Object> resultMap = getElementValueByName(root, elementName);
        return (String) resultMap.get("elementValue");
    }

    public static HashMap<String, Object> getElementValueByName(Node node, String elementName) {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("found", "false");
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap != null) {
            for (int j = 0; j < nodeMap.getLength(); j++) {
                String attrName = nodeMap.item(j).getNodeName();
                String attrValue = nodeMap.item(j).getNodeValue();
                if ("FriendlyName".equalsIgnoreCase(attrName) && elementName.equalsIgnoreCase(attrValue)) {
                    resultMap.put("elementValue", node.getTextContent());
                    resultMap.put("found", "true");
                    break;
                }
            }
        }

        if ("true".equals(resultMap.get("found"))) {
            return resultMap;
        } else {
            NodeList childList = node.getChildNodes();
            if (childList != null && childList.getLength() > 0) {
                for (int i = 0; i < childList.getLength(); i++) {
                    Node childElement = childList.item(i);
                    resultMap = getElementValueByName(childElement, elementName);
                    if ("true".equals(resultMap.get("found"))) {
                        break;
                    }
                }
            }
        }
        return resultMap;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getNotBefore() {
        return notBefore;
    }

    @Override
    public String getNotOnOrAfter() {
        return notOnOrAfter;
    }

    @Override
    public Node getSignature() {
        return signature;
    }

    @Override
    public String getNameId() {
        return nameId;
    }

    @Override
    public String getCn() {
        return cn;
    }

    @Override
    public String getSn() {
        return sn;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public String getEmployeeType() {
        return employeeType;
    }

    @Override
    public String getRegisterPhone() {
        return registerPhone;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    @Override
    public String getMail() {
        return mail;
    }

    @Override
    public String getDepartmentName() {
        return departmentName;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public UserInfoBean getUIBean() {
        return uiBean;
    }

    private String getIssuer(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS("*", "Issuer");
        if (nl.getLength() != 0) {
            return nl.item(0).getTextContent();
        }
        return null;
    }

    private Element getCondition(Document doc) throws SAMLException {
        NodeList nl = doc.getElementsByTagNameNS("*", "Conditions");
        if (nl.getLength() != 0) {
            return (Element) nl.item(0);
        }
        return null;
    }

    private Node getSignature(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() != 0) {
            return nl.item(0);
        }
        return null;
    }

    private String getNameId(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS("*", "NameID");
        if (nl.getLength() != 0) {
            return nl.item(0).getTextContent();
        }
        return null;
    }
}
