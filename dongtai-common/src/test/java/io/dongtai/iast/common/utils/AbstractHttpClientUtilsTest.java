package io.dongtai.iast.common.utils;

import io.dongtai.iast.common.enums.HttpMethods;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class AbstractHttpClientUtilsTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final boolean oldEnableColor = DongTaiLog.enableColor;

    private static final String BASE_URL = "https://iast-test.huoxian.cn";

    private void clear() {
        outputStreamCaptor.reset();
    }

    @Before
    public void setUp() {
        DongTaiLog.enablePrintLog = true;
        DongTaiLog.enableColor = false;
        clear();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        DongTaiLog.enablePrintLog = false;
        DongTaiLog.enableColor = oldEnableColor;
        clear();
        System.setOut(standardOut);
    }

    @Test
    public void sendRequest() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        String url;
        String data;
        StringBuilder resp;
        JSONObject respObj;
        int status;

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
        Assert.assertEquals("captcha/refresh status", 202, status);
    }

    @Test
    public void testDownloadFile() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Token foo");
        String url = "https://iast-test.huoxian.cn/openapi/api/v1/engine/download?engineName=dongtai-api";
        boolean ok = AbstractHttpClientUtils.downloadFile(url, "/tmp/agent.jar", headers, "", -1);
        Assert.assertFalse("invalid token download", ok);
        String log = outputStreamCaptor.toString();
        Assert.assertTrue("invalid token download error", log.contains("[ERROR]") && log.contains("download failed"));
    }
}