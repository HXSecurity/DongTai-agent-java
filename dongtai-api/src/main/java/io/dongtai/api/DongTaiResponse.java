package io.dongtai.api;

import java.io.IOException;
import java.util.Map;

public interface DongTaiResponse {

    public Map<String, Object> getResponseMeta(boolean getBody);

    public byte[] getResponseData() throws IOException;
}
