package io.dongtai.api.servlet2;

import io.dongtai.api.DongTaiRequest;
import io.dongtai.log.DongTaiLog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServletRequestWrapper extends HttpServletRequestWrapper implements DongTaiRequest {


    private String body;
    private final boolean usingBody;
    private final boolean isPostMethod;
    private boolean isCachedBody;

    public ServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.isPostMethod = "POST".equals(getMethod());
        this.usingBody = isPostMethod && allowedContentType(request.getContentType());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.usingBody) {
            if (!isCachedBody) {
                InputStream inputStream = super.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = null;
                try {
                    if (inputStream != null) {
                        String ce = getCharacterEncoding();
                        if (null == ce || ce.isEmpty()) {
                            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        } else {
                            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, ce));
                        }
                        char[] charBuffer = new char[128];
                        int bytesRead;
                        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                            stringBuilder.append(charBuffer, 0, bytesRead);
                        }
                    }
                    assert bufferedReader != null;
                    bufferedReader.close();
                } catch (IOException e) {
                    // fixme: add logger for solve exception
                    DongTaiLog.error(e);
                }
                body = stringBuilder.toString();
                isCachedBody = true;
            }
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());

            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                /**
                 * fixme: add method body
                 * @param readListener
                 */
                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (usingBody) {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        } else {
            return super.getReader();
        }
    }

    @Override
    public Map<String, Object> getRequestMeta() {
        Map<String, Object> requestMeta = new HashMap<String, Object>(16);
        requestMeta.put("contextPath", this.getContextPath());
        requestMeta.put("servletPath", this.getServletPath());
        requestMeta.put("requestURL", this.getRequestURL());
        requestMeta.put("requestURI", this.getRequestURI());
        requestMeta.put("method", this.getMethod());
        requestMeta.put("serverName", this.getServerName());
        requestMeta.put("serverPort", this.getServerPort());
        requestMeta.put("queryString", this.getQueryString());
        requestMeta.put("protocol", this.getProtocol());
        requestMeta.put("scheme", this.getScheme());
        requestMeta.put("remoteAddr", getDongTaiRemoteAddr());
        requestMeta.put("secure", this.isSecure());
        requestMeta.put("body", "");
        requestMeta.put("headers", getHeaders());
        requestMeta.put("replay-request", null != this.getHeader("dongtai-replay-id"));
        return requestMeta;
    }

    public Map<String, String> getHeaders() {
        Enumeration<?> headerNames = this.getHeaderNames();
        Map<String, String> headers = new HashMap<>(32);
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            String value = this.getHeader(name);
            headers.put(name, value);
        }
        return headers;
    }

    /**
     * parse HttpRequest to read POST Body
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public String getPostBody() {
        StringBuilder postBody = new StringBuilder();
        try {
            if (!isPostMethod) {
                return postBody.toString();
            }

            if (isCachedBody) {
                return body;
            }

            if (usingBody) {
                this.getInputStream();
                return body;
            } else {
                Enumeration<?> parameterNames = this.getParameterNames();
                String param;
                boolean first = true;
                while (parameterNames.hasMoreElements()) {
                    param = (String) parameterNames.nextElement();
                    if (first) {
                        first = false;
                        postBody.append(param).append("=").append(this.getParameter(param));
                    } else {
                        postBody.append("&").append(param).append("=").append(this.getParameter(param));
                    }
                }
            }
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
        return postBody.toString();
    }

    public String getDongTaiRemoteAddr() {
        String remoteAddr = super.getRemoteAddr();
        return remoteAddr.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : remoteAddr;
    }
}
