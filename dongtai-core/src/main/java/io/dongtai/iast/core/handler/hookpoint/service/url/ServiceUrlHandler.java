package io.dongtai.iast.core.handler.hookpoint.service.url;

import java.util.List;

public interface ServiceUrlHandler {
    List<ServiceUrl> processUrl(String host, String port);
}
