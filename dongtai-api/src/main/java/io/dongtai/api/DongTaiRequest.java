package io.dongtai.api;

import java.util.Map;

public interface DongTaiRequest {

    public Map<String, Object> getRequestMeta();

    public String getPostBody();

    public default boolean allowedContentType(String contentType) {
        return contentType != null && (contentType.contains("application/json")
                || contentType.contains("application/xml"));
    }
}
