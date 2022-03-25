package io.dongtai.api.jakarta;


import io.dongtai.api.DongTaiResponse;
import io.dongtai.log.DongTaiLog;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author owefsad
 */
public class JakartaResponseWrapper extends HttpServletResponseWrapper implements DongTaiResponse {

    private ServletOutputStream outputStream;
    private JakartaWrapperOutputStreamCopier copier;

    public JakartaResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader("DongTai", "v1.4.0");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new JakartaWrapperOutputStreamCopier(outputStream);
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

    private String getLine() {
        return "HTTP/1.1" + " " + getStatus() + "\n";
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
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
        return new byte[0];
    }
}
