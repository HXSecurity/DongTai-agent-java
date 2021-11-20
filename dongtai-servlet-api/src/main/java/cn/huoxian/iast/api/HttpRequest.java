package cn.huoxian.iast.api;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpRequest {

    public static Map<String, Object> getRequest(Object req) {
        HttpServletRequest request = (HttpServletRequest) req;
        Map<String, Object> requestMeta = new HashMap<String, Object>(16);
        requestMeta.put("contextPath", request.getContextPath());
        requestMeta.put("servletPath", request.getServletPath());
        requestMeta.put("requestURL", request.getRequestURL());
        requestMeta.put("requestURI", request.getRequestURI());
        requestMeta.put("method", request.getMethod());
        requestMeta.put("serverName", request.getServerName());
        requestMeta.put("serverPort", request.getServerPort());
        requestMeta.put("queryString", request.getQueryString());
        requestMeta.put("protocol", request.getProtocol());
        requestMeta.put("scheme", request.getScheme());
        requestMeta.put("remoteAddr", getRemoteAddr(request));
        requestMeta.put("secure", request.isSecure());
        requestMeta.put("body", getPostBody(request));
        requestMeta.put("headers", getHeaders(request));
        requestMeta.put("replay-request", null != request.getHeader("dongtai-replay-id"));

        return requestMeta;
    }

    private static String getHeaders(HttpServletRequest request) {
        Enumeration<?> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            headers.append(name).append(":").append(request.getHeader(name)).append("\n");
        }
        return headers.toString();
    }

    /**
     * fixme 解析inputStream获取请求体
     *
     * @param request
     * @return
     */
    private static String getPostBody(HttpServletRequest request) {
        StringBuilder postBody = new StringBuilder();
        try {
            if ("POST".equals(request.getMethod())) {
                boolean usingBody = false;
                if (request.getContentType() != null) {
                    usingBody = request.getContentType().contains("application/json");
                }
                if (usingBody) {
                    InputStream inputStream = request.getInputStream();
                    InputStreamReader isReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(isReader);

                    String str;
                    while ((str = reader.readLine()) != null) {
                        postBody.append(str);
                    }
                    return postBody.toString();
                } else {
                    Enumeration<?> parameterNames = request.getParameterNames();
                    String param;
                    boolean first = true;
                    while (parameterNames.hasMoreElements()) {
                        param = (String) parameterNames.nextElement();
                        if (first) {
                            first = false;
                            postBody.append(param).append("=").append(request.getParameter(param));
                        } else {
                            postBody.append("&").append(param).append("=").append(request.getParameter(param));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postBody.toString();
    }

    private static String getRemoteAddr(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : remoteAddr;
    }

}
