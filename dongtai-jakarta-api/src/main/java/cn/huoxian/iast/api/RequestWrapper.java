package cn.huoxian.iast.api;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author owefsad
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private final String body;
    private final boolean usingBody;
    private final Map<String, String> customHeaders;

    public static Object cloneRequest(Object req) {
        if (req instanceof HttpServletRequest) {
            return new RequestWrapper((HttpServletRequest) req);
        }
        return req;
    }

    private RequestWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<String, String>();
        this.usingBody = ("POST".equals(request.getMethod()) && request.getContentType().contains("application/json"));

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        if (this.usingBody) {
            try {
                InputStream inputStream = request.getInputStream();
                if (inputStream != null) {
                    String ce = request.getCharacterEncoding();
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
                e.printStackTrace();
            }
        }
        body = stringBuilder.toString();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (usingBody) {
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

                @Override
                public void setReadListener(ReadListener readListener) {

                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        } else {
            return super.getInputStream();
        }
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
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);

        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<String>(customHeaders.keySet());

        @SuppressWarnings("unchecked")
        Enumeration<String> headerNames = super.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            set.add(headerNames.nextElement());
        }
        return Collections.enumeration(set);
    }

    public String getBody() {
        return this.body;
    }
}
