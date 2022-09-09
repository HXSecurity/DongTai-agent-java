package io.dongtai.api.servlet2;

import io.dongtai.api.DongTaiResponse;
import io.dongtai.log.DongTaiLog;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.*;

public class ServletResponseWrapper extends HttpServletResponseWrapper implements DongTaiResponse {

    private ServletOutputStream outputStream = null;
    private PrintWriter writer = null;
    private ServletWrapperOutputStreamCopier copier = null;

    public ServletResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader("DongTai", "v1.7.6");
    }

    private String getLine() {
        return "HTTP/1.1" + " " + getStatus() + "\n";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            DongTaiLog.error("getOutputStream() has already been called over once");
        }
        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new ServletWrapperOutputStreamCopier(outputStream);
        }
        return copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            DongTaiLog.error("getWriter() has already been called over once");
        }
        if (writer == null) {
            copier = new ServletWrapperOutputStreamCopier(getResponse().getOutputStream());
            writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (copier != null) {
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
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
        if (copier != null) {
            return copier.getCopy();
        } else {
            return new byte[0];
        }
    }
}
