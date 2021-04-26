package com.secnium.iast.core.handler.models;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastReplayModel {
    public IastReplayModel(String requestMethod, String requestUrl, String queryString, String requestBody, String requestHeader, String traceId) {
        this.requestMethod = requestMethod.toUpperCase();
        this.requestUrl = requestUrl;
        this.requestQueryString = queryString;
        this.requestBody = requestBody;
        this.requestHeader = requestHeader;
        this.traceId = traceId;
        this.fullUrl = queryString == null ? requestUrl : requestUrl + "?" + queryString;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRequestQueryString(String requestQueryString) {
        this.requestQueryString = requestQueryString;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getFullUrl() {
        return this.fullUrl;
    }

    public String getRequestQueryString() {
        return requestQueryString;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public String getTraceId() {
        return traceId;
    }

    public boolean queryStringIsEmpty() {
        return null == requestQueryString || requestQueryString.trim().isEmpty();
    }

    private String requestHeader;
    private String traceId;
    private String requestMethod;
    private String requestUrl;
    private String requestQueryString;
    private String requestBody;
    private String fullUrl;
}
