package io.dongtai.iast.core.handler.hookpoint.models;

public class KrpcApiModel {

    String hosts;
    String path;
    String methods;
    String serviceId;
    String msgId;

    public KrpcApiModel() {
    }

    public KrpcApiModel(String hosts, String path, String methods, String serviceId, String msgId) {
        this.hosts = hosts;
        this.path = path;
        this.methods = methods;
        this.serviceId = serviceId;
        this.msgId = msgId;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
