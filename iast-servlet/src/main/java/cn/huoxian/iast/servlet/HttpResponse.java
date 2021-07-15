package cn.huoxian.iast.servlet;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    public static Map<String, Object> getResponse(Object res) {
        HttpServletResponse response = (HttpServletResponse) res;
        Map<String, Object> responseMeta = new HashMap<String, Object>(16);
        return responseMeta;
    }

    private static String getHeaders(HttpServletResponse response) {
        StringBuilder header = new StringBuilder();
        return header.toString();
    }

    private static String getBody(HttpServletResponse response) {
        return "";
    }
}
