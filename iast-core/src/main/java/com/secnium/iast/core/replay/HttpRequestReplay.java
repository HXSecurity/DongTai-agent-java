package com.secnium.iast.core.replay;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.handler.models.IASTReplayModel;
import com.secnium.iast.core.handler.vulscan.overpower.AuthInfoCache;
import com.secnium.iast.core.util.HttpClientHostnameVerifier;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.HttpMethods;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 根据字符串格式的header头及cookie信息生成重放请求
 * 问题：
 * 1. 触发sql查询时，未出现多余的身份验证信息（Cookie)，此时作何处理？
 * 方案：
 * - 忽略当前请求的越权检测，将导致检测结果的不稳定（偶发性检测，不建议）
 * - 临时保存当前请求，当出现多个cookie时，替换cookie进行检查（数据如何存储？）
 * 2. 重放权限信息时，全部重放还是重放其中的一个？
 * 背景；如果重放多个，将导致网络IO增加，可能影响服务器的正常运行，如何解决该问题？如果重放一个，可能导致漏洞检出不准确的问题
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpRequestReplay extends AbstractThread {
    private static final String PROTOCOL_HTTPS = "https";
    public final static HostnameVerifier DO_NOT_VERIFY = new HttpClientHostnameVerifier();
    private final static ConcurrentLinkedQueue<IASTReplayModel> WAITING_REPLAY_REQUESTS = new ConcurrentLinkedQueue<IASTReplayModel>();

    public HttpRequestReplay() {
        this(null, true, 0);
    }

    protected HttpRequestReplay(String name, boolean enable, long waitTime) {
        super(name, enable, waitTime);
    }

    public static void sendReplayRequest(IASTReplayModel replayModel) {
        WAITING_REPLAY_REQUESTS.offer(replayModel);
    }


    /**
     * 发起重放请求
     */
    private static void doReplay(IASTReplayModel replayModel) {
        // 准备http请求需要的数据（url、post数据、headers头）
        HashMap<String, String> headers = splitHeaderStringToHashmap(replayModel.getRequestHeader(), replayModel.getTraceId());
        // 发送http请求
        try {
            String cookieValue = headers.get("Cookie");
            headers.put("Cookie", "");
            sendRequest(replayModel.getRequestMethod(), replayModel.getFullUrl(), replayModel.getRequestBody(), headers);
            headers.put("Cookie", cookieValue);
            sendRequest(replayModel.getRequestMethod(), replayModel.getFullUrl(), replayModel.getRequestBody(), headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> splitHeaderStringToHashmap(String originalHeaders, String traceId) {
        HashMap<String, String> headers = new HashMap<String, String>();
        String[] headerItems = originalHeaders.trim().split("\n");
        for (String item : headerItems) {
            int splitCharIndex = item.indexOf(":");
            String key = item.substring(0, splitCharIndex);
            String value = item.substring(splitCharIndex + 1);
            // 替换cookie
            if ("cookie".equals(key.toLowerCase())) {
                value = AuthInfoCache.getAnotherCookie(value);
            }
            headers.put(key, value);
        }

        headers.put("x-trace-id", traceId);
        return headers;
    }

    /**
     * 发起HTTP请求的重放
     *
     * @param method  http请求的方法
     * @param fullUrl http请求的地址
     * @param data    http请求的数据，用于post请求
     * @param headers http请求的header头
     * @throws Exception http请求中抛出的异常
     */
    private static void sendRequest(String method, String fullUrl, String data, HashMap<String, String> headers) throws Exception {
        HttpURLConnection connection = null;
        try {
            HttpClientUtils.trustAllHosts();
            URL url = new URL(fullUrl);
            if (PROTOCOL_HTTPS.equals(url.getProtocol().toLowerCase())) {
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                connection = https;
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestMethod(method);
            if (null != headers) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            //Send request
            connection.getResponseCode();
            if (HttpMethods.POST.equals(method)) {
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void send() {
        while (!WAITING_REPLAY_REQUESTS.isEmpty()) {
            IASTReplayModel model = WAITING_REPLAY_REQUESTS.poll();
            doReplay(model);
        }
    }
}
