package io.dongtai.iast.core.handler.hookpoint;

public class IastServer {
    private String serverAddr;
    private Integer serverPort;
    private String protocol;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    private boolean inited;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public IastServer(String serverAddr, Integer serverPort, String protocol, boolean inited) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.protocol = protocol;
        this.inited = inited;
    }
}
