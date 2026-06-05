/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import lombok.Data;

import java.util.Objects;

@Data
public class KerberosAuth {

    String hostNames;

    String clusterIps;

    String port;

    String protocol;

    String userKeytabFile;

    String krb5File;

    public Boolean isValid() {
        Objects.requireNonNull(clusterIps, "clusterIps should not be null");
        Objects.requireNonNull(port, "port should not be null");
        Objects.requireNonNull(protocol, "protocol should not be null");
        return true;
    }
}
