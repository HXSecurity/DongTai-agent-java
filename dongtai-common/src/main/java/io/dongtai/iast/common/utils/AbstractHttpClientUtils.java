package io.dongtai.iast.common.utils;

import io.dongtai.iast.common.enums.HttpMethods;
import io.dongtai.log.DongTaiLog;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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

        return sendRequest(client, method, url, data, headers, handler);
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

        return sendRequest(client, m, url, data, headers, null);
    }

    protected static StringBuilder sendRequest(CloseableHttpClient client, HttpMethods method, String url, String data,
                                               Map<String, String> headers, HttpClientExceptionHandler handler) {
        StringBuilder response = new StringBuilder();
        CloseableHttpResponse resp = null;

        try {
            if (method.equals(HttpMethods.GET)) {
                HttpGet req = new HttpGet(url);
                resp = sendRequestInternal(client, req, data, headers, handler);
            } else {
                HttpPost req = new HttpPost(url);
                resp = sendRequestInternal(client, req, data, headers, handler);
            }
            if (resp != null) {
                if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    DongTaiLog.error("request {} response status code invalid: {}",
                            url, resp.getStatusLine().getStatusCode());
                }

                response.append(EntityUtils.toString(resp.getEntity(), "UTF-8"));
                DongTaiLog.trace("dongtai request url is {}, request is {}, response is {}",
                        url, data, response.toString());
                return response;
            }
        } catch (Exception e) {
            DongTaiLog.error("request " + url + " parse response failed", e);
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
     * @param data    post data
     * @param headers headers map
     * @return StringBuilder
     * <p>
     * java.net.URLConnection will not shut down the threads properly, so we use apache httpclient
     * https://stackoverflow.com/questions/33849053/how-to-stop-a-url-connection-upon-thread-interruption-java
     */
    private static CloseableHttpResponse sendRequestInternal(CloseableHttpClient client, HttpRequestBase req,
                                                             String data, Map<String, String> headers,
                                                             HttpClientExceptionHandler func) {
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.setHeader(entry.getKey(), entry.getValue());
                }
            }

            if (req instanceof HttpPost && data != null) {
                GzipCompressingEntity entity = new GzipCompressingEntity(new StringEntity(data));
                ((HttpPost) req).setEntity(entity);
            }
            return client.execute(req);
        } catch (IOException e) {
            DongTaiLog.error("request " + req.getURI().toString() + " failed", e);
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
                DongTaiLog.error("The remote file {} response empty", fileURL);
                return false;
            }

            String contentType = resp.getFirstHeader(HEADER_CONTENT_TYPE).getValue();
            if (MEDIA_TYPE_APPLICATION_JSON.equals(contentType)
                    || MEDIA_TYPE_TEXT_PLAIN.equals(contentType)
                    || MEDIA_TYPE_TEXT_HTML.equals(contentType)) {
                String r = EntityUtils.toString(resp.getEntity(), "UTF-8");
                DongTaiLog.error("The remote file {} download failed. response code: {}, body: {}",
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
        } catch (Exception e) {
            DongTaiLog.error("The remote file " + fileURL + " download failure", e);
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
