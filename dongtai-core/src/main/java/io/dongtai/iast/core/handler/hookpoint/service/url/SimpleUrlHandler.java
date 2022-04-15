package io.dongtai.iast.core.handler.hookpoint.service.url;

import java.util.ArrayList;
import java.util.List;

public class SimpleUrlHandler implements ServiceUrlHandler {
    @Override
    public List<ServiceUrl> processUrl(String host, String port) {
        List<ServiceUrl> urls = new ArrayList<ServiceUrl>();
        if (!host.isEmpty() && !port.isEmpty()) {
            urls.add(new ServiceUrl(host, port));
        }
        return urls;
    }
}
