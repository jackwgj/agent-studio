package com.openjiuwen.studio.agent.space.common.utils.tls;

import com.openjiuwen.studio.agent.space.common.context.ThreadLocalContext;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.LogUtils;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

@Slf4j
public class X509TrustManagerWrapper extends X509ExtendedTrustManager {
    private final List<X509ExtendedTrustManager> trustManagers = new ArrayList<>();

    private final String trustStoreFile;

    private final String filterIp;

    public X509TrustManagerWrapper(PKIXBuilderParameters pkixParams, String trustStoreFile, String filterIp)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(new CertPathTrustManagerParameters(pkixParams));
        TrustManager[] trustManagerArray = tmf.getTrustManagers();

        Arrays.asList(trustManagerArray).forEach(item -> {
            if (item instanceof X509ExtendedTrustManager) {
                this.trustManagers.add((X509ExtendedTrustManager) item);
            }
        });
        this.trustStoreFile = trustStoreFile;
        this.filterIp = filterIp;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(chain, authType, socket);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
        String serverIp = "";
        String hostName = "";
        try {
            InetAddress serverAddress = socket.getInetAddress();
            serverIp = serverAddress.getHostAddress();
            hostName = serverAddress.getHostName();
        } catch (Exception exception) {
            log.error("get https server ip and host failed.");
        }
        if (StringUtil.isNotEmptyString(filterIp)) {
            String[] ips = filterIp.split(",");
            for (String ip : ips) {
                if (StringUtil.isNotEmptyString(ThreadLocalContext.getInstance().getUrl())
                    && ThreadLocalContext.getInstance().getUrl().contains(ip)) {
                    ThreadLocalContext.getInstance().setUrl(null);
                    log.info("no tls check.");
                    return;
                }
                if (serverIp.contains(ip) || hostName.contains(ip)) {
                    log.info("no tls check by white name filter.");
                    return;
                }
            }
        }
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(chain, authType, socket);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(chain, authType, engine);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        if (StringUtil.isNotEmptyString(filterIp) && StringUtil.isNotEmptyString(
            ThreadLocalContext.getInstance().getUrl())) {
            String[] ips = filterIp.split(",");
            for (String ip : ips) {
                if (ThreadLocalContext.getInstance().getUrl().contains(ip)) {
                    ThreadLocalContext.getInstance().setUrl(null);
                    return;
                }
            }
        }
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(chain, authType, engine);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(chain, authType);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        if (StringUtil.isNotEmptyString(filterIp) && StringUtil.isNotEmptyString(
            ThreadLocalContext.getInstance().getUrl())) {
            String[] ips = filterIp.split(",");
            for (String ip : ips) {
                if (ThreadLocalContext.getInstance().getUrl().contains(ip)) {
                    ThreadLocalContext.getInstance().setUrl(null);
                    return;
                }
            }
        }
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(chain, authType);
            }
        } catch (GeneralSecurityException e) {
            log.error("cert check fail, error message:{}", LogUtils.encodeForLog(e.getMessage()));
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        } catch (Throwable e) {
            log.error("cert check error", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SSL_CHECK_FAILED);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> certificateList = new ArrayList<>();
        this.trustManagers.forEach(item -> certificateList.addAll(Arrays.asList(item.getAcceptedIssuers())));
        return certificateList.toArray(new X509Certificate[certificateList.size()]);
    }
}
