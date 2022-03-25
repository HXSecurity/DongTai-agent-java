package io.dongtai.api.servlet2;

import io.dongtai.api.DongTaiResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServletResponseWrapper extends HttpServletResponseWrapper implements DongTaiResponse {

    private ServletOutputStream outputStream = null;
    private ServletWrapperOutputStreamCopier copier = null;

    public ServletResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader("DongTai", "v1.4.0");
    }

    private String getLine() {
        return "HTTP/1.1" + " " + getStatus() + "\n";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new ServletWrapperOutputStreamCopier(outputStream);
        }
        return copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return getResponse().getWriter();
    }

    @Override
    public void flushBuffer() throws IOException {
        if (copier != null) {
            copier.flush();
        }
    }

    @Override
    public Map<String, Object> getResponseMeta() {
        Map<String, Object> responseMeta = new HashMap<String, Object>(2);
        responseMeta.put("headers", getHeaders());
        responseMeta.put("body", getResponseData());
        return responseMeta;
    }

    private String getHeaders() {
        StringBuilder header = new StringBuilder();
        Collection<String> headerNames = getHeaderNames();
        header.append(getLine());
        for (String headerName : headerNames) {
            header.append(headerName).append(":").append(getHeader(headerName)).append("\n");
        }
        return header.toString();
    }

    @Override
    public byte[] getResponseData() {
        try {
            flushBuffer();
            if (copier != null) {
                return copier.getCopy();
            }
        } catch (Exception ignored) {

        }
        return new byte[0];
    }
}
