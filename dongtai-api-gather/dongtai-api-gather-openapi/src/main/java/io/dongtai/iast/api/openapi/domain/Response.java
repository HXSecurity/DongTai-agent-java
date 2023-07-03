package io.dongtai.iast.api.openapi.domain;

import java.util.Map;

/**
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class Response {

    public static final String CODE_OK = "200";
    public static final String MSG_OK = "ok";

    // TODO 不同的响应内容

    // 响应的描述信息
    private String description;

    // 响应头
    private Map<String, Header> headers;

    // 响应内容
    private Map<String, MediaType> content;

    // 此字段不要
//    private Map<String, Link> links;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Header> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    public Map<String, MediaType> getContent() {
        return content;
    }

    public void setContent(Map<String, MediaType> content) {
        this.content = content;
    }
}
