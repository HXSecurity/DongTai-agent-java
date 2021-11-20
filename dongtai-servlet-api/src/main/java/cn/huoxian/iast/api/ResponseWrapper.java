package cn.huoxian.iast.api;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private WrapperOutputStreamCopier copier;

    public static Object cloneResponse(Object response) {
        if (response instanceof HttpServletResponse) {
            return new ResponseWrapper((HttpServletResponse) response);
        }
        return response;
    }

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader("DongTai", "v1.1.1");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getOutputStream() has already been called over once");
        }
        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new WrapperOutputStreamCopier(outputStream);
        }
        return copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getWriter() has already been called over once");
        }
        if (writer == null) {
            copier = new WrapperOutputStreamCopier(getResponse().getOutputStream());
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

    public byte[] getResponseData() throws IOException {
        flushBuffer();
        if (copier != null) {
            return copier.getCopy();
        } else {
            return new byte[0];
        }
    }
}
