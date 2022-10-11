package io.dongtai.iast.agent.util;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.common.enums.HttpMethods;
import io.dongtai.iast.common.utils.AbstractHttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpClientUtils extends AbstractHttpClientUtils {
    private final static IastProperties PROPERTIES = IastProperties.getInstance();
    private static String proxyHost = "";
    private static int proxyPort = -1;

    static {
        if (PROPERTIES.isProxyEnable()) {
            proxyHost = PROPERTIES.getProxyHost();
            proxyPort = PROPERTIES.getProxyPort();
        }
    }

    public static StringBuilder sendGet(String uri, Map<String, String> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder uriBuilder = new StringBuilder(uri);
            uriBuilder.append("?");
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                uriBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            uri = uriBuilder.toString();
        }

        Map<String, String> headers = new HashMap<String, String>();
        setToken(headers);

        return sendRequest(HttpMethods.GET, PROPERTIES.getBaseUrl() + uri, null, headers, 0,
                proxyHost, proxyPort, null);
    }

    public static StringBuilder sendPost(String uri, String value) {
        Map<String, String> headers = new HashMap<String, String>();
        setToken(headers);
        headers.put(HEADER_CONTENT_TYPE, MEDIA_TYPE_APPLICATION_JSON);
        headers.put(HEADER_CONTENT_ENCODING, REQUEST_ENCODING_TYPE);

        return sendRequest(HttpMethods.POST, PROPERTIES.getBaseUrl() + uri, value, headers, 0,
                proxyHost, proxyPort, null);
    }

    public static boolean downloadRemoteJar(String fileURI, String fileName) {
        Map<String, String> headers = new HashMap<String, String>();
        setToken(headers);

        return downloadFile(PROPERTIES.getBaseUrl() + fileURI, fileName, headers, proxyHost, proxyPort);
    }

    private static void setToken(Map<String, String> headers) {
        headers.put(REQUEST_HEADER_TOKEN_KEY, "Token " + PROPERTIES.getServerToken());
    }
}
