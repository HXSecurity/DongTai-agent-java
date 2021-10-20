package cn.huoxian.iast.spring;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RequestWrapper extends HttpServletRequestWrapper {
    private final String body;
    private final boolean usingBody;

    public static Object cloneRequest(Object req) {
        if (req instanceof HttpServletRequest) {
            return new RequestWrapper((HttpServletRequest) req);
        }
        return req;
    }

    private RequestWrapper(HttpServletRequest request) {
        super(request);
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

    public String getBody() {
        return this.body;
    }
}
