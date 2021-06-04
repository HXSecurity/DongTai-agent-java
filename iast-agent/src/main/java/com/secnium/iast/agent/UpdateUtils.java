package com.secnium.iast.agent;

import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class UpdateUtils {
    private final static IastProperties properties = IastProperties.getInstance();
    private final static String UPDATE_URL = properties.getBaseUrl() + "/api/v1/engine/update";
    private final static String START_URL = properties.getBaseUrl() + "/api/v1/engine/startstop";
    private final static String AGENT_TOKEN = URLEncoder.encode(AgentRegister.getAgentToken());

    public static boolean checkForUpdate() {
        String respRaw = sendRequest(UPDATE_URL + "?agent_name=" + AGENT_TOKEN);
        System.out.println(respRaw);
        if (respRaw != null && !respRaw.isEmpty()) {
            JSONObject resp = new JSONObject(respRaw);
            return "1".equals(resp.get("data").toString());
        }
        return false;
    }

    public static String checkForStatus() {
        try {
            String respRaw = sendRequest(START_URL + "?agent_name=" + AGENT_TOKEN);
            System.out.println(respRaw);
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                return resp.get("data").toString();
            }
        } catch (Exception e) {
            return "other";
        }
        return "other";
    }

    public static void setUpdateSuccess() {
        sendRequest(UPDATE_URL + "/0?agent_name=" + AGENT_TOKEN);
    }

    public static void setUpdateFailure() {
        sendRequest(UPDATE_URL + "/1?agent_name=" + AGENT_TOKEN);
    }

    private static String sendRequest(String urlStr) {
        HttpURLConnection connection = null;
        try {
            trustAllHosts();
            URL url = new URL(urlStr);
            Proxy proxy = UpdateUtils.loadProxy();

            if (Constant.PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection https = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
                https.setHostnameVerifier(DO_NOT_VERIFY);
                connection = https;
            } else {
                connection = proxy == null ? (HttpURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection(proxy);
            }
            connection.setRequestMethod(Constant.HTTP_METHOD_GET);
            connection.setRequestProperty("User-Agent", "SecniumIast Java Agent");
            connection.setRequestProperty("Authorization", "Token " + properties.getIastServerToken());
            connection.setRequestProperty("Accept", "*/*");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            StringBuilder sb = new StringBuilder();
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                sb.append(new String(dataBuffer, 0, bytesRead));
            }

            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance(Constant.SSL_INSTANCE);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    /**
     * 根据配置文件创建http/https代理
     */
    public static Proxy loadProxy() {
        try {
            if (properties.isProxyEnable()) {
                Proxy proxy;
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                        properties.getProxyHost(),
                        properties.getProxyPort()
                ));
                return proxy;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }


    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };
}
