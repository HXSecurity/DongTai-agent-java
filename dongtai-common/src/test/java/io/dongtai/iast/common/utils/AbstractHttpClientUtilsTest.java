package io.dongtai.iast.common.utils;

import io.dongtai.iast.common.enums.HttpMethods;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class AbstractHttpClientUtilsTest {
    private final static String LS = System.getProperty("line.separator");
    private static final String TITLE = "[io.dongtai.iast.agent] ";
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final boolean oldEnableColor = DongTaiLog.ENABLE_COLOR;

    private static final String BASE_URL = "http://114.132.191.62:8000";

    private void clear() {
        outputStreamCaptor.reset();
    }

    @Before
    public void setUp() {
        DongTaiLog.ENABLED = true;
        DongTaiLog.ENABLE_COLOR = false;
        clear();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        DongTaiLog.ENABLED = false;
        DongTaiLog.ENABLE_COLOR = oldEnableColor;
        clear();
        System.setOut(standardOut);
    }

    /**
     * 暂时发现服务端也就是POC环境没有对这些登陆做特殊处理，故无法通过测试用例，暂时注释，待发现问题后详细说明
     */
//    @Test
    public void sendRequest() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        String url;
        String data;
        StringBuilder resp;
        JSONObject respObj;
        int status;
        String log;

        url = BASE_URL + "/api/v1/captcha/refresh";
        resp = AbstractHttpClientUtils.sendRequest(HttpMethods.GET, url, null, headers, 0, "", -1, null);
        respObj = new JSONObject(resp.toString());
        status = respObj.getInt("status");
        Assert.assertEquals("captcha/refresh status", 201, status);

        url = BASE_URL + "/api/v1/user/login";
        data = "{\"username\":\"test\",\"password\":\"test\",\"captcha\":\"test\",\"captcha_hash_key\":\"test\"}";
        resp = AbstractHttpClientUtils.sendRequest(HttpMethods.POST, url, data, headers, 0, "", -1, null);
        respObj = new JSONObject(resp.toString());
        status = respObj.getInt("status");
        Assert.assertEquals("user/login status", 202, status);

        url = BASE_URL + "/api/v1/profiles";
        AbstractHttpClientUtils.sendRequest(HttpMethods.GET, url, data, headers, 0, "", -1, null);
        log = outputStreamCaptor.toString();
        int code = ErrorCode.HTTP_CLIENT_REQUEST_RESPONSE_CODE_INVALID.getCode();
        String fmt = String.format(ErrorCode.HTTP_CLIENT_REQUEST_RESPONSE_CODE_INVALID.getMessage().replaceAll("\\{\\}", "%s"),
                url, "401");
        Assert.assertEquals("invalid openapi token", log.substring(20),
                TITLE + "[WARN] [" + code + "] " + fmt + LS);
        clear();

        url = BASE_URL + ":55555";
        final String exMsg = "custom exception handler";
        HttpClientBuilder hcb = AbstractHttpClientUtils.getClientBuilder(0, "", -1);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .build();
        hcb.setDefaultRequestConfig(requestConfig);
        CloseableHttpClient client = hcb.build();
        resp = AbstractHttpClientUtils.sendRequest(client, HttpMethods.GET, url, null, headers, new AbstractHttpClientUtils.HttpClientExceptionHandler() {
            @Override
            public void run() {
                clear();
                System.out.println(exMsg);
            }
        });
        log = outputStreamCaptor.toString();
        Assert.assertEquals("exception handler resp", "", resp.toString());
        Assert.assertEquals("exception handler", exMsg, log.trim());
    }

//    @Test
    public void testDownloadFile() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Token foo");
        String url = BASE_URL + "/openapi/api/v1/engine/download?engineName=dongtai-api";
        boolean ok = AbstractHttpClientUtils.downloadFile(url, "/tmp/agent.jar", headers, "", -1);
        Assert.assertFalse("invalid token download", ok);
        String log = outputStreamCaptor.toString();
        int code = ErrorCode.HTTP_CLIENT_REMOTE_FILE_RESPONSE_INVALID.getCode();
        String fmt = String.format(ErrorCode.HTTP_CLIENT_REMOTE_FILE_RESPONSE_INVALID.getMessage()
                .replaceAll("\\{\\}", "%s"), url, "401", "");
        Assert.assertTrue("invalid token download error",
                log.substring(20).startsWith(TITLE + "[ERROR] [" + code + "] " + fmt));
        clear();
    }
}