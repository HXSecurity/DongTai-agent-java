package io.dongtai.iast.core.handler.hookpoint.service.url;

import java.util.ArrayList;
import java.util.List;

public class KafkaUrlHandler implements ServiceUrlHandler {
    @Override
    public List<ServiceUrl> processUrl(String host, String port) {
        List<ServiceUrl> urls = new ArrayList<ServiceUrl>();
        String[] hosts = host.split(",");
        for (String url : hosts) {
            if (url.isEmpty()) {
                continue;
            }
            String[] hostAndPort = url.split(":");
            if (hostAndPort.length == 2 && !hostAndPort[0].isEmpty() && !hostAndPort[1].isEmpty()) {
                urls.add(new ServiceUrl(hostAndPort[0], hostAndPort[1]));
            }
        }
        return urls;
    }
}
