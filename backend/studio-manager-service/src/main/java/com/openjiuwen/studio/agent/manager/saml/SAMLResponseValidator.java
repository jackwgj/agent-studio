/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml;

public interface SAMLResponseValidator {
    void validate() throws SAMLException;

    String getNameId();

    String getCn();

    String getSn();

    String getGivenName();

    String getDisplayName();

    String getEmployeeNumber();

    String getEmployeeType();

    String getUid();

    String getUuid();

    String getTelephoneNumber();

    String getMail();

    String getDepartmentName();

    String getSource();

    String getRegisterPhone();

    UserInfoBean getUIBean();
}
