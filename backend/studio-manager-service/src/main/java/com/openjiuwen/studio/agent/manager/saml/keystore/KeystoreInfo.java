/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class KeystoreInfo {
    PublicKey publicKey = null;

    PrivateKeyEntry privateKeyEntry = null;

    public KeystoreInfo(InputStream jks, char[] storepass, char[] keypass, String alias) throws KeyInfoException {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(jks, storepass);
            privateKeyEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(storepass));
            if (ks.containsAlias(alias)) {
                Key key = ks.getKey(alias, storepass);
                if (key instanceof PrivateKey) {
                    Certificate cert = ks.getCertificate(alias);
                    publicKey = cert.getPublicKey();
                } else {
                    throw new KeyInfoException("Private Key Not Exist");
                }
            } else {
                throw new KeyInfoException("Alias Not Exist");
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
            | UnrecoverableEntryException e) {
            throw new KeyInfoException(e.getMessage(), e);
        }
    }

    public PrivateKeyEntry getPrivateKeyEntry() {
        return privateKeyEntry;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

}
