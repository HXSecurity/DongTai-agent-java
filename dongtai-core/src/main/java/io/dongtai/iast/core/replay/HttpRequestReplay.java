package io.dongtai.iast.core.replay;

import io.dongtai.iast.core.handler.hookpoint.models.IastReplayModel;
import io.dongtai.iast.core.utils.HttpClientHostnameVerifier;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.iast.core.utils.HttpMethods;
import io.dongtai.iast.core.utils.base64.Base64Decoder;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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
public class HttpRequestReplay implements Runnable {
    private static final String PROTOCOL_HTTPS = "https";
    public final static HostnameVerifier DO_NOT_VERIFY = new HttpClientHostnameVerifier();
    private final StringBuilder replayRequestRaw;

    public HttpRequestReplay(StringBuilder replayRequestRaw) {
        this.replayRequestRaw = replayRequestRaw;
    }


    /**
     * 发起重放请求
     */
    private static void doReplay(IastReplayModel replayModel) {
        try {
            HashMap<String, String> headers = splitHeaderStringToHashmap(replayModel.getRequestHeader());
            headers.put("dongtai-replay-id", String.valueOf(replayModel.getReplayId()));
            headers.put("dongtai-relation-id", String.valueOf(replayModel.getRelationId()));
            headers.put("dongtai-replay-type", String.valueOf(replayModel.getReplayType()));

            String url = replayModel.getFullUrl();
            if (url != null) {
                sendRequest(replayModel.getRequestMethod(), url, replayModel.getRequestBody(), headers);
            }
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
    }

    private static HashMap<String, String> splitHeaderStringToHashmap(String originalHeaders) {
        HashMap<String, String> headers = new HashMap<String, String>(32);
        byte[] headerRaw = Base64Decoder.decodeBase64FromString(originalHeaders);
        if (headerRaw != null) {
            String decodeHeaders = new String(headerRaw);
            String[] headerItems = decodeHeaders.trim().split("\n");
            for (String item : headerItems) {
                int splitCharIndex = item.indexOf(":");
                if (splitCharIndex > 0) {
                    String key = item.substring(0, splitCharIndex);
                    String value = item.substring(splitCharIndex + 1);
                    headers.put(key, value);
                }
            }
        }

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
            if (HttpMethods.POST.equals(method)) {
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(data.getBytes(Charset.forName("UTF-8")));
                outputStream.close();
            }

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void run() {
        try {
            JSONObject resp = new JSONObject(replayRequestRaw.toString());
            Integer statusCode = (Integer) resp.get("status");
            if (statusCode == 201) {
                String data = resp.get("data").toString();
                if (!"[]".equals(data)) {
                    JSONArray replayRequests = (JSONArray) resp.get("data");
                    for (int index = 0, total = replayRequests.length(); index < total; index++) {
                        JSONObject replayRequest = (JSONObject) replayRequests.get(index);
                        IastReplayModel replayModel = new IastReplayModel(
                                replayRequest.get("method"),
                                replayRequest.get("uri"),
                                replayRequest.get("params"),
                                replayRequest.get("body"),
                                replayRequest.get("header"),
                                replayRequest.get("id"),
                                replayRequest.get("relation_id"),
                                replayRequest.get("replay_type"));
                        if (replayModel.isValid()) {
                            doReplay(replayModel);
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
        }
    }
}
