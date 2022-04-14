package io.dongtai.iast.core.handler.hookpoint.service.url;

public class ServiceUrl {
    private String host;

    private String port;

    public ServiceUrl(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServiceUrl{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
