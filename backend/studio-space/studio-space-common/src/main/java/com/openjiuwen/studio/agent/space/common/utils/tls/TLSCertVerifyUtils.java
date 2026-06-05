package com.openjiuwen.studio.agent.space.common.utils.tls;

import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.SecureRandomUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRL;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

@Slf4j
public class TLSCertVerifyUtils {
    /**
     * @param trustStoreFile     证书文件路径
     * @param trustStoreType     证书文件类型，比如jks 详细参考
     * @param trustStorePassword 证书文件保护口令，可以为空
     * @param crlFile            证书吊销列表，可以为空
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws CertificateException
     */
    public static SSLContext buildSSLContext(String trustStoreFile, String trustStoreType, String trustStorePassword,
        String crlFile, String filterIp)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, KeyManagementException,
        CertificateException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(buildKeystoreManagers(trustStoreFile, trustStoreType, trustStorePassword, crlFile),
            buildTrustManagers(trustStoreFile, trustStoreType, trustStorePassword, crlFile, filterIp),
            SecureRandomUtil.getInstance());
        return sslContext;
    }

    public static KeyManager[] buildKeystoreManagers(String trustStoreFile, String trustStoreType,
        String trustStorePassword, String crlFile)
        throws NoSuchAlgorithmException, KeyStoreException, CertificateException {
        if (!StringUtils.hasText(trustStoreFile)) {
            throw new IllegalArgumentException("param trustStoreFile can not be null");
        }
        if (!StringUtils.hasText(trustStoreType)) {
            throw new IllegalArgumentException("param trustStoreType can not be null");
        }
        char[] pwd = null;
        if (StringUtils.hasText(trustStorePassword)) {
            pwd = trustStorePassword.toCharArray();
        }
        KeyStore keyStore = KeyStore.getInstance(trustStoreType);
        try (FileInputStream trustStoreInput = new FileInputStream(FileUtils.getFile(trustStoreFile))) {
            keyStore.load(trustStoreInput, pwd);
        } catch (IOException ex) {
            log.error("key store failed!", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.LOAD_KEY_STORE_FAILED);
        }
        Collection<CRL> crls = null;
        if (StringUtils.hasText(crlFile)) {
            try (FileInputStream crlInput = new FileInputStream(FileUtils.getFile(crlFile))) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                crls = new HashSet<>();
                crls.add(certFactory.generateCRL(crlInput));
            } catch (Exception ex) {
                log.error("crls add failed!", ex);
                throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
            }
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        try {
            keyManagerFactory.init(keyStore, trustStorePassword.toCharArray());
        } catch (UnrecoverableKeyException e) {
            log.error("init key store manager factory failed.");
        }
        return keyManagerFactory.getKeyManagers();
    }

    /**
     * @param trustStoreFile     证书文件路径
     * @param trustStoreType     证书文件类型，比如jks 详细参考
     * @param trustStorePassword 证书文件保护口令，可以为空
     * @param crlFile            证书吊销列表，可以为空
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws KeyStoreException
     * @throws CertificateException
     */
    public static TrustManager[] buildTrustManagers(String trustStoreFile, String trustStoreType,
        String trustStorePassword, String crlFile, String filterIp)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException {
        if (!StringUtils.hasText(trustStoreFile)) {
            throw new IllegalArgumentException("param trustStoreFile can not be null");
        }
        if (!StringUtils.hasText(trustStoreType)) {
            throw new IllegalArgumentException("param trustStoreType can not be null");
        }

        char[] pwd = null;
        if (StringUtils.hasText(trustStorePassword)) {
            pwd = trustStorePassword.toCharArray();
        }

        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        try (FileInputStream trustStoreInput = new FileInputStream(FileUtils.getFile(trustStoreFile))) {
            trustStore.load(trustStoreInput, pwd);
        } catch (IOException ex) {
            log.error("Trust Store Load Failed", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }

        Collection<CRL> crls = null;
        if (StringUtils.hasText(crlFile)) {
            try (FileInputStream crlInput = new FileInputStream(FileUtils.getFile(crlFile))) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                crls = new HashSet<>();
                crls.add(certFactory.generateCRL(crlInput));
            } catch (Exception ex) {
                log.error("crls add failed!", ex);
                throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
            }
        }
        return getTrustManagers(trustStoreFile, trustStore, crls, filterIp);
    }

    @SuppressWarnings("sunapi")
    private static TrustManager[] getTrustManagers(String trustStoreFile, KeyStore trustStore, Collection<CRL> crls,
        String filterIp) throws InvalidAlgorithmParameterException, KeyStoreException, NoSuchAlgorithmException {
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
        if (null != crls) {
            List<CertStore> certStores = new ArrayList<>();
            certStores.add(CertStore.getInstance("Collection", new CollectionCertStoreParameters(crls)));

            CertPathBuilder certPathBuilder = CertPathBuilder.getInstance(CertPathBuilder.getDefaultType());
            PKIXRevocationChecker revocateChecker = (PKIXRevocationChecker) certPathBuilder.getRevocationChecker();
            revocateChecker.setOptions(
                EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS, PKIXRevocationChecker.Option.NO_FALLBACK));

            pkixParams.setRevocationEnabled(true);
            pkixParams.setCertStores(certStores);
            pkixParams.addCertPathChecker(revocateChecker);
        } else {
            pkixParams.setRevocationEnabled(false);
        }

        TrustManager trustManager = new X509TrustManagerWrapper(pkixParams, trustStoreFile, filterIp);
        return new TrustManager[] {trustManager};
    }

    public static HostnameVerifier getHostnameVerifierByBlacklist(String... hostnames) {
        Set<String> blacklist = new HashSet<>(Arrays.asList(hostnames));
        return (hostname, sslSession) -> !blacklist.contains(hostname);
    }
}