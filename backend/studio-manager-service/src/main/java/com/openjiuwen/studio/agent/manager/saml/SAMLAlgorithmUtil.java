/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.saml;

import java.util.EnumMap;

@SuppressWarnings("unchecked")
public class SAMLAlgorithmUtil {

    static EnumMap<DigestAlgorithmEnum, String> digestAlgorithmEnumMap = new EnumMap<>(DigestAlgorithmEnum.class);

    static EnumMap<SignatureAlgorithmEnum, String> signatureAlgorithmEnumMap = new EnumMap<>(
        SignatureAlgorithmEnum.class);

    static {

        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.SHA1, "http://www.w3.org/2000/09/xmldsig#sha1");
        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.SHA224, "http://www.w3.org/2001/04/xmldsig-more#sha224");
        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.SHA256, "http://www.w3.org/2001/04/xmlenc#sha256");
        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.SHA384, "http://www.w3.org/2001/04/xmldsig-more#sha384");
        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.SHA512, "http://www.w3.org/2001/04/xmlenc#sha512");
        digestAlgorithmEnumMap.put(DigestAlgorithmEnum.RIPEMD160, "http://www.w3.org/2001/04/xmlenc#ripemd160");

        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.DSA_SHA1, "http://www.w3.org/2000/09/xmldsig#dsa-sha1");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_SHA1, "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_SHA224,
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_SHA256,
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_SHA384,
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_SHA512,
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.RSA_RIPEMD160,
            "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.ECDSA_SHA1,
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.ECDSA_SHA224,
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.ECDSA_SHA256,
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.ECDSA_SHA384,
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.ECDSA_SHA512,
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512");
        signatureAlgorithmEnumMap.put(SignatureAlgorithmEnum.DSA_SHA256, "http://www.w3.org/2009/xmldsig11#dsa-sha256");

    }

    public static String getDigestAlgorithmValue(String key) {
        DigestAlgorithmEnum algorithm = getDigestAlgorithmEnum(key);
        return digestAlgorithmEnumMap.get(algorithm);
    }

    private static DigestAlgorithmEnum getDigestAlgorithmEnum(String key) {
        DigestAlgorithmEnum digestAlgorithmEnum = DigestAlgorithmEnum.SHA1;

        for (DigestAlgorithmEnum algorithmEnum : DigestAlgorithmEnum.values()) {
            String algorithmKey = algorithmEnum.getKey();
            if (algorithmKey.equals(key)) {
                digestAlgorithmEnum = algorithmEnum;
                break;
            }
        }
        return digestAlgorithmEnum;
    }

    public static String getSignatureAlgorithmValue(String key) {
        SignatureAlgorithmEnum algorithm = getSignatureAlgorithmEnum(key);
        return signatureAlgorithmEnumMap.get(algorithm);
    }

    private static SignatureAlgorithmEnum getSignatureAlgorithmEnum(String key) {
        // 默认RSA_SHA1
        SignatureAlgorithmEnum signatureAlgorithmEnum = SignatureAlgorithmEnum.RSA_SHA1;

        for (SignatureAlgorithmEnum algorithmEnum : SignatureAlgorithmEnum.values()) {
            String algorithmKey = algorithmEnum.getKey();
            if (algorithmKey.equals(key)) {
                signatureAlgorithmEnum = algorithmEnum;
                break;
            }
        }
        return signatureAlgorithmEnum;
    }

    public enum DigestAlgorithmEnum {
        SHA1("SHA1"),
        SHA224("SHA224"),
        SHA256("SHA256"),
        SHA384("SHA384"),
        SHA512("SHA512"),
        RIPEMD160("RIPEMD160") {
        };

        private final String key;

        DigestAlgorithmEnum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum SignatureAlgorithmEnum {
        DSA_SHA1("DSA_SHA1"),
        RSA_SHA1("RSA_SHA1"),
        RSA_SHA224("RSA_SHA224"),
        RSA_SHA256("RSA_SHA256"),
        RSA_SHA384("RSA_SHA384"),
        RSA_SHA512("RSA_SHA512"),
        RSA_RIPEMD160("RSA_RIPEMD160"),
        ECDSA_SHA1("ECDSA_SHA1"),
        ECDSA_SHA224("ECDSA_SHA224"),
        ECDSA_SHA256("ECDSA_SHA256"),
        ECDSA_SHA384("ECDSA_SHA384"),
        ECDSA_SHA512("ECDSA_SHA512"),
        DSA_SHA256("DSA_SHA256") {
        };

        private final String key;

        SignatureAlgorithmEnum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

}
