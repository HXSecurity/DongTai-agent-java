package io.dongtai.iast.common.utils;

import io.dongtai.iast.common.enums.HttpMethods;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.http.*;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.Map;

public class AbstractHttpClientUtils {
    protected static final String REQUEST_HEADER_TOKEN_KEY = "Authorization";
    protected static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    protected static final String REQUEST_ENCODING_TYPE = "gzip";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    protected static final String MEDIA_TYPE_TEXT_PLAIN = "text/plain";
    protected static final String MEDIA_TYPE_TEXT_HTML = "text/html";

    protected interface HttpClientExceptionHandler {
        void run();
    }

    protected static StringBuilder sendRequest(HttpMethods method, String url, String data, Map<String, String> headers,
                                               int maxRetries, String proxyHost, int proxyPort,
                                               HttpClientExceptionHandler handler) {
        CloseableHttpClient client = getClient(maxRetries, proxyHost, proxyPort);

        HttpEntity reqBody = null;
        try {
            if (HttpMethods.POST.equals(method) && data != null && !data.isEmpty()) {
                reqBody = new GzipCompressingEntity(new StringEntity(data, "UTF-8"));
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_PREPARE_REQUEST_BODY_FAILED"), url, e);
        }
        StringBuilder response = sendRequest(client, method, url, reqBody, headers, handler);
        DongTaiLog.trace("dongtai request url is {}, request is {}, response is {}",
                url, data, response.toString());
        return response;
    }

    public static StringBuilder sendReplayRequest(String method, String url, String data, Map<String, String> headers) {
        StringBuilder response = new StringBuilder();
        CloseableHttpClient client = getReplayClient();
        HttpMethods m;
        if (HttpMethods.GET.equals(method)) {
            m = HttpMethods.GET;
        } else if (HttpMethods.POST.equals(method)) {
            m = HttpMethods.POST;
        } else {
            return response;
        }

        HttpEntity reqBody = null;
        try {
            if (HttpMethods.POST.equals(method) && data != null && !data.isEmpty()) {
                String contentType = headers.get("content-type");
                if (contentType == null || contentType.isEmpty()) {
                    reqBody = new StringEntity(data, "UTF-8");
                } else if (!contentType.contains(";")) {
                    reqBody = new StringEntity(data, ContentType.create(contentType, "UTF-8"));
                } else {
                    reqBody = new StringEntity(data, ContentType.parse(contentType));
                }
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("HTTP_CLIENT_PREPARE_REQUEST_BODY_FAILED"), url, e);
            return response;
        }
        response = sendRequest(client, m, url, reqBody, headers, null);
        DongTaiLog.trace("dongtai replay request url is {}, request is {}, response is {}",
                url, data, response.toString());
        return response;
    }

    protected static StringBuilder sendRequest(CloseableHttpClient client, HttpMethods method, String url, HttpEntity reqBody,
                                               Map<String, String> headers, HttpClientExceptionHandler handler) {
        StringBuilder response = new StringBuilder();
        CloseableHttpResponse resp = null;

        try {
            if (method.equals(HttpMethods.GET)) {
                HttpGet req = new HttpGet(url);
                resp = sendRequestInternal(client, req, reqBody, headers, handler);
            } else {
                HttpPost req = new HttpPost(url);
                resp = sendRequestInternal(client, req, reqBody, headers, handler);
            }
            if (resp != null) {
                if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    DongTaiLog.warn(ErrorCode.get("HTTP_CLIENT_REQUEST_RESPONSE_CODE_INVALID"),
                            url, resp.getStatusLine().getStatusCode());
                }

                response.append(EntityUtils.toString(resp.getEntity(), "UTF-8"));
                return response;
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_REQUEST_PARSE_RESPONSE_FAILED"), url, e);
            if (handler != null) {
                handler.run();
            }
        } finally {
            if (resp != null) {
                try {
                    resp.close();
                } catch (IOException ignore) {
                }
            }

            if (client != null) {
                try {
                    client.close();
                } catch (IOException ignore) {
                }
            }
        }
        return response;
    }

    /**
     * @param req     HttpRequestBase
     * @param reqBody post data
     * @param headers headers map
     * @return StringBuilder
     * <p>
     * java.net.URLConnection will not shut down the threads properly, so we use apache httpclient
     * https://stackoverflow.com/questions/33849053/how-to-stop-a-url-connection-upon-thread-interruption-java
     */
    private static CloseableHttpResponse sendRequestInternal(CloseableHttpClient client, HttpRequestBase req,
                                                             HttpEntity reqBody, Map<String, String> headers,
                                                             HttpClientExceptionHandler func) {
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.setHeader(entry.getKey(), entry.getValue());
                }
            }

            if (req instanceof HttpPost && reqBody != null) {
                ((HttpPost) req).setEntity(reqBody);
            }
            return client.execute(req);
        } catch (IOException e) {
            DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_REQUEST_EXECUTE_FAILED"), req.getURI().toString(), e);
            if (func != null) {
                func.run();
            }
        }
        return null;
    }

    public static CloseableHttpClient getClient(int maxRetries, String proxyHost, int proxyPort) {
        HttpClientBuilder hcb = getClientBuilder(maxRetries, proxyHost, proxyPort);
        return hcb.build();
    }

    public static HttpClientBuilder getClientBuilder(int maxRetries, String proxyHost, int proxyPort) {
        HttpClientBuilder hcb = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        if (maxRetries > 0) {
            hcb.setRetryHandler(new DefaultHttpRequestRetryHandler(10, false));
        } else {
            hcb.disableAutomaticRetries();
        }
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            hcb.setProxy(new HttpHost(proxyHost, proxyPort));
        }
        hcb.setUserAgent("DongTai-IAST-Agent");
        return hcb;
    }

    public static CloseableHttpClient getReplayClient() {
        HttpClientBuilder hcb = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .disableAutomaticRetries();
        return hcb.build();
    }

    /**
     * Download file
     *
     * @param fileURL  file url
     * @param fileName local file name
     */
    protected static boolean downloadFile(String fileURL, String fileName, Map<String, String> headers,
                                          String proxyHost, int proxyPort) {
        CloseableHttpClient client = null;
        CloseableHttpResponse resp = null;
        try {
            client = getClient(0, proxyHost, proxyPort);

            HttpGet req = new HttpGet(fileURL);
            resp = sendRequestInternal(client, req, null, headers, null);
            if (resp == null) {
                DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_REMOTE_FILE_RESPONSE_EMPTY"), fileURL);
                return false;
            }

            String contentType = resp.getFirstHeader(HEADER_CONTENT_TYPE).getValue();
            if (MEDIA_TYPE_APPLICATION_JSON.equals(contentType)
                    || MEDIA_TYPE_TEXT_PLAIN.equals(contentType)
                    || MEDIA_TYPE_TEXT_HTML.equals(contentType)) {
                String r = EntityUtils.toString(resp.getEntity(), "UTF-8");
                DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_REMOTE_FILE_RESPONSE_INVALID"),
                        fileURL, resp.getStatusLine().getStatusCode(), r);
                return false;
            }

            BufferedInputStream in = new BufferedInputStream(resp.getEntity().getContent());
            final File classPath = new File(new File(fileName).getParent());

            if (!classPath.mkdirs() && !classPath.exists()) {
                DongTaiLog.info("Check or create local file cache path, path is {}", classPath);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            dataBuffer = null;
            in.close();
            fileOutputStream.close();
            DongTaiLog.info("The remote file {} was successfully written to the local file {}", fileURL, fileName);
            return true;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("HTTP_CLIENT_REMOTE_FILE_DOWNLOAD_FAILED"), fileURL, e);
        } finally {
            if (resp != null) {
                try {
                    resp.close();
                } catch (IOException ignore) {
                }
            }

            if (client != null) {
                try {
                    client.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }
}
