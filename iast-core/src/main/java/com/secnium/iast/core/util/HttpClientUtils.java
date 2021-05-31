package com.secnium.iast.core.util;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.report.ErrorLogReport;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpClientUtils {
    private static final String PROTOCOL_HTTPS = "https";
    private static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String REQUEST_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String REQUEST_HEADER_CONTENT_ENCODING = "Content-Encoding";
    private static final String REQUEST_HEADER_USER_AGENT = "user-agent";
    private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    private static final String REQUEST_HEADER_TOKEN_KEY = "Authorization";
    private static final String REQUEST_ENCODING_TYPE = "gzip";
    private static final String SSL_SIGNATURE = "TLS";
    public final static HostnameVerifier DO_NOT_VERIFY = new HttpClientHostnameVerifier();
    private final static PropertyUtils PROPERTIES = PropertyUtils.getInstance();
    private final static Proxy PROXY = loadProxy();

    public static String sendGet(String uri, String arg, String value) {
        try {
            if (arg != null && value != null) {
                return sendRequest(HttpMethods.GET, PROPERTIES.getBaseUrl(), uri + "?" + arg + "=" + value, null, null, PROXY);
            } else {
                return sendRequest(HttpMethods.GET, PROPERTIES.getBaseUrl(), uri, null, null, PROXY);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean sendPost(String uri, String value) throws Exception {
        Asserts.NOT_NULL("report", value);
        if(PROPERTIES.isDebug()){
            String respString = sendRequest(HttpMethods.POST, PROPERTIES.getBaseUrl(), uri, value, null, PROXY);
            System.out.println("cn.huoxian.iast url is " + uri);
            System.out.println("cn.huoxian.iast resp is " + respString);
        }
        sendRequest(HttpMethods.POST, PROPERTIES.getBaseUrl(), uri, value, null, PROXY);
        return true;
    }


    private static String sendRequest(HttpMethods method, String baseUrl, String urlStr, String data, HashMap<String, String> headers, Proxy proxy) throws Exception {
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        try {
            trustAllHosts();
            URL url = new URL(baseUrl + urlStr);
            // 通过请求地址判断请求类型(http或者是https)
            if (PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection https = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
                https.setHostnameVerifier(DO_NOT_VERIFY);
                connection = https;
            } else {
                connection = proxy == null ? (HttpURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection(proxy);
            }

            connection.setRequestMethod(method.name());
            if (HttpMethods.POST.equals(method)) {
                connection.setRequestProperty(REQUEST_HEADER_CONTENT_TYPE, MEDIA_TYPE_APPLICATION_JSON);
                connection.setRequestProperty(REQUEST_HEADER_CONTENT_LENGTH, Integer.toString(data.getBytes().length));
                connection.setRequestProperty(REQUEST_HEADER_CONTENT_ENCODING, REQUEST_ENCODING_TYPE);
                connection.setRequestProperty(REQUEST_HEADER_USER_AGENT, "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36 IAST-AGENT");
            }
            //fixme:根据配置文件动态获取token和http请求头，用于后续自定义操作
            connection.setRequestProperty(REQUEST_HEADER_USER_AGENT, "SecniumIast Agent");
            connection.setRequestProperty(REQUEST_HEADER_TOKEN_KEY, "Token " + PropertyUtils.getInstance().getIastServerToken());
            // 插入自定义的
            if (null != headers) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            //Send request
            if (HttpMethods.POST.equals(method)) {
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                String encryptData = RsaUtils.encrypt(data);
                GZIPOutputStream wr = new GZIPOutputStream(connection.getOutputStream());
                wr.write(encryptData.getBytes(Charset.forName("UTF-8")));
                wr.close();
            }
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 根据配置文件创建http/https代理
     */
    public static Proxy loadProxy() {
        try {
            if (PROPERTIES.isProxyEnable()) {
                Proxy proxy;
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                        PROPERTIES.getProxyHost(),
                        PROPERTIES.getProxyPort()
                ));
                return proxy;
            }
        } catch (Throwable ignored) {

        }
        return null;
    }

    public static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new IastTrustManager()};
        try {
            SSLContext sc = SSLContext.getInstance(SSL_SIGNATURE);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }


}
