package io.dongtai.api;

import java.util.Map;

public interface DongTaiRequest {

    public Map<String, Object> getRequestMeta();

    public String getPostBody();

}
