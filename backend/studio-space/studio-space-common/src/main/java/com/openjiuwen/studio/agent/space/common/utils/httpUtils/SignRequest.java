package com.openjiuwen.studio.agent.space.common.utils.httpUtils;

import com.openjiuwen.studio.agent.space.common.constant.HttpMethodName;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignRequest {
    @Getter
    private String key = null;
    private String secret = null;
    private String method = null;
    private String url = null;
    @Getter
    @Setter
    private String body = null;
    @Getter
    private String fragment = null;
    @Getter
    private final Map<String, String> headers = new Hashtable<>();
    private final Map<String, List<String>> queryString = new Hashtable<>();
    private static final Pattern PATTERN = Pattern.compile("^(?i)(post|put|patch|delete|get|options|head)$");

    public SignRequest() {
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getRegion() {
        return "";
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getServiceName() {
        return "";
    }

    public String getSecrect() {
        return this.secret;
    }

    public HttpMethodName getMethod() {
        return HttpMethodName.valueOf(this.method.toUpperCase(Locale.getDefault()));
    }

    public void setAppKey(String appKey) {
        if (null != appKey && !appKey.trim().isEmpty()) {
            this.key = appKey;
        } else {
            throw new AgentSpaceException("appKey can not be empty");
        }
    }

    public void setAppSecrect(String appSecret) {
        if (null != appSecret && !appSecret.trim().isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new AgentSpaceException("appSecrect can not be empty");
        }
    }

    public void setKey(String appKey) {
        if (null != appKey && !appKey.trim().isEmpty()) {
            this.key = appKey;
        } else {
            throw new AgentSpaceException("appKey can not be empty");
        }
    }

    public void setSecret(String appSecret) {
        if (null != appSecret && !appSecret.trim().isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new AgentSpaceException("appSecrect can not be empty");
        }
    }

    public void setMethod(String method) {
        if (null == method) {
            throw new AgentSpaceException("method can not be empty");
        } else {
            Matcher match = PATTERN.matcher(method);
            if (!match.matches()) {
                throw new AgentSpaceException("unsupported method");
            } else {
                this.method = method;
            }
        }
    }

    public String getUrl() throws UnsupportedEncodingException {
        StringBuilder uri = new StringBuilder();
        uri.append(this.url);
        if (!this.queryString.isEmpty()) {
            uri.append("?");
            int loop = 0;

            for (Map.Entry<String, List<String>> entry : this.queryString.entrySet()) {
                for (String value : entry.getValue()) {
                    if (loop > 0) {
                        uri.append("&");
                    }

                    uri.append(UrlUtil.urlEncode(entry.getKey(), false));
                    uri.append("=");
                    uri.append(UrlUtil.urlEncode(value, false));
                    ++loop;
                }
            }
        }

        if (this.fragment != null) {
            uri.append("#");
            uri.append(this.fragment);
        }

        return uri.toString();
    }

    public void setUrl(String urlRet) {
        if (urlRet != null && !urlRet.trim().isEmpty()) {
            int i = urlRet.indexOf(35);
            if (i >= 0) {
                urlRet = urlRet.substring(0, i);
            }

            i = urlRet.indexOf(63);
            this.url = urlRet;
            if (i >= 0) {
                String query = urlRet.substring(i + 1);

                for (String item : query.split("&")) {
                    String[] spl = item.split("=", 2);
                    String keyRet = spl[0];
                    String value = "";
                    if (spl.length > 1) {
                        value = spl[1];
                    }

                    if (!keyRet.trim().isEmpty()) {
                        keyRet = URLDecoder.decode(keyRet, StandardCharsets.UTF_8);
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                        this.addQueryStringParam(keyRet, value);
                    }
                }

                urlRet = urlRet.substring(0, i);
                this.url = urlRet;
            }
        } else {
            throw new AgentSpaceException("url can not be empty");
        }
    }

    public String getPath() {
        String urlRet = this.url;
        int i = urlRet.indexOf("://");
        if (i >= 0) {
            urlRet = urlRet.substring(i + 3);
        }

        i = urlRet.indexOf(47);
        return i >= 0 ? urlRet.substring(i) : "/";
    }

    public String getHost() {
        String urlRet = this.url;
        int i = urlRet.indexOf("://");
        if (i >= 0) {
            urlRet = urlRet.substring(i + 3);
        }

        i = urlRet.indexOf(47);
        if (i >= 0) {
            urlRet = urlRet.substring(0, i);
        }

        return urlRet;
    }

    public void addQueryStringParam(String name, String value) {
        List<String> paramList = this.queryString.computeIfAbsent(name, k -> new ArrayList<>());
        paramList.add(value);
    }

    public Map<String, List<String>> getQueryStringParams() {
        return this.queryString;
    }

    public void setFragment(String fragment) {
        if (fragment != null && !fragment.trim().isEmpty()) {
            this.fragment = URLEncoder.encode(fragment, StandardCharsets.UTF_8);
        } else {
            throw new AgentSpaceException("fragment can not be empty");
        }
    }

    public void addHeader(String name, String value) {
        if (name != null && !name.trim().isEmpty()) {
            this.headers.put(name, value);
        }
    }
}
