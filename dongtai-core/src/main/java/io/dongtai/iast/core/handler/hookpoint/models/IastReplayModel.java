package io.dongtai.iast.core.handler.hookpoint.models;

import io.dongtai.iast.core.EngineManager;

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
        this.valid = true;
    }

    public IastReplayModel(Object method, Object uri, Object queryString, Object body, Object header, Object id, Object relationId, Object replayType) {
        try {
            this.requestMethod = (String) method;
            this.requestUrl = (String) uri;
            this.requestQueryString = (String) queryString;
            this.requestBody = (String) body;
            this.requestHeader = (String) header;
            this.replayId = (Integer) id;
            this.relationId = (Integer) relationId;
            this.replayType = (Integer) replayType;
            this.valid = true;
        } catch (Throwable e) {
            this.valid = false;
        }
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
        if (EngineManager.SERVER == null) {
            return null;
        }
        String host = EngineManager.SERVER.getServerAddr().concat(":").concat(String.valueOf(EngineManager.SERVER.getServerPort()));

        StringBuilder url = new StringBuilder();
        // fixme 根据协议，判断使用http/https
        if (EngineManager.SERVER.getProtocol().toLowerCase().contains("https")){
            url.append("https://");
        }else {
            url.append("http://");
        }
        url.append(host);
        if (getRequestQueryString().isEmpty()) {
            url.append(requestUrl);
        } else {
            url.append(requestUrl).append("?").append(requestQueryString);
        }
        return url.toString();
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

    public Boolean isValid() {
        return this.valid;
    }

    private String requestHeader;
    private String traceId;
    private String requestMethod;
    private String requestUrl;
    private String requestQueryString;
    private String requestBody;
    private String fullUrl;
    private Boolean valid;

    public Integer getReplayId() {
        return replayId;
    }

    public Integer getRelationId() {
        return relationId;
    }

    public Integer getReplayType() {
        return replayType;
    }

    private Integer replayId;
    private Integer relationId;
    private Integer replayType;
}
