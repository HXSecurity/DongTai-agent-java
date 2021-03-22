package com.secnium.iast.core.util.http;

import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.ThrowableUtils;

import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpResponse {

    private Collection<String> headerNames = new ArrayList<String>();
    private HashMap<String, String> headers = new HashMap<String, String>();
    private OutputStream os;

    private WeakReference<Object> response;

    private Method methodOfGetHeaderNames;
    private Method methodOfGetHeaders;
    private Method methodOfGetHeader;
    private Method methodOfGetOutputStream;

    public HttpResponse(Object response) {
        this.setResponse(new WeakReference<Object>(response));
    }

    public void initResponse() {
        try {
            methodOfGetHeaderNames = response.get().getClass().getMethod("getHeaderNames");
            methodOfGetHeaderNames.setAccessible(true);
            methodOfGetHeaders = response.get().getClass().getMethod("getHeaders", String.class);
            methodOfGetHeaders.setAccessible(true);
            methodOfGetHeader = response.get().getClass().getMethod("getHeader", String.class);
            methodOfGetHeader.setAccessible(true);
            methodOfGetOutputStream = response.get().getClass().getMethod("getOutputStream");
            methodOfGetOutputStream.setAccessible(true);

            try {
                // 设置响应头
                this.setHeaders((Collection<String>) methodOfGetHeaderNames.invoke(response.get()));
                // 设置响应体
                this.setOutputStream((OutputStream) methodOfGetOutputStream.invoke(response.get()));
            } catch (IllegalAccessException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (InvocationTargetException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        } catch (NoSuchMethodException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public Object getResponse() {
        return response.get();
    }

    public void setResponse(WeakReference<Object> response) {
        this.response = response;
    }

    public void setHeaders(Collection<String> headerNames) {
        Iterator<String> it = headerNames.iterator();

        while (it.hasNext()) {
            String key = it.next();
            this.headerNames.add(key);
            try {
                this.headers.put(key, (String) methodOfGetHeader.invoke(this.response.get(), key));
            } catch (IllegalAccessException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (InvocationTargetException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaderNames(Collection<String> headerNames) {
        this.headerNames = headerNames;
    }

    public Collection<String> getHeaderNames() {
        return this.headerNames;
    }

    public void addHeader(String key, String value) {
        if (response.get() != null) {
            try {
                Object responseObj = response.get();
                if (null != responseObj) {
                    responseObj.getClass().getMethod("addHeader", String.class, String.class).invoke(response.get(), key, value);
                }
            } catch (NoSuchMethodException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (IllegalAccessException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (InvocationTargetException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
    }

    public void setOutputStream(OutputStream os) {
        this.os = os;
    }
}
