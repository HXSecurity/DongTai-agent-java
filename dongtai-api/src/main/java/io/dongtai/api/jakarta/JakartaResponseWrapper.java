package io.dongtai.api.jakarta;


import io.dongtai.api.DongTaiResponse;
import io.dongtai.iast.common.config.*;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.log.DongTaiLog;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author owefsad
 */
public class JakartaResponseWrapper extends HttpServletResponseWrapper implements DongTaiResponse {

    private ServletOutputStream outputStream;
    private JakartaWrapperOutputStreamCopier copier;

    @SuppressWarnings("unchecked")
    public JakartaResponseWrapper(HttpServletResponse response) {
        super(response);
        try {
            boolean enableVersionHeader = ((Config<Boolean>) ConfigBuilder.getInstance()
                    .getConfig(ConfigKey.ENABLE_VERSION_HEADER)).get();
            if (enableVersionHeader) {
                String versionHeaderKey = ((Config<String>) ConfigBuilder.getInstance()
                        .getConfig(ConfigKey.VERSION_HEADER_KEY)).get();
                response.addHeader(versionHeaderKey, AgentConstant.VERSION_VALUE);
            }
        } catch (Throwable ignore) {
        }
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
    public Map<String, Object> getResponseMeta(boolean getBody) {
        Map<String, Object> responseMeta = new HashMap<String, Object>(2);
        responseMeta.put("headers", getHeaders());
        responseMeta.put("body", getResponseData(getBody));
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
    public byte[] getResponseData(boolean getBody) {
        try {
            flushBuffer();
            if (getBody && copier != null) {
                return copier.getCopy();
            }
        } catch (Throwable e) {
            DongTaiLog.error("get jakarta response data failed", e);
        }
        return new byte[0];
    }
}
