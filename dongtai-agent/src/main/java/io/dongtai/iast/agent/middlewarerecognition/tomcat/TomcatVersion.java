package io.dongtai.iast.agent.middlewarerecognition.tomcat;

/**
 * @author dongzhiyong@huoxian.cn
 */

public enum TomcatVersion {
    /**
     *
     */
    V5("v5.x", "Tomcat/5.x"),
    V6("v6.x", "Tomcat/6.x"),
    V7("v7.x", "Tomcat/7.x"),
    V8("v8.x", "Tomcat/8.x"),
    V9("v9.x", "Tomcat/9.x");

    private final String version;
    private final String displayname;

    TomcatVersion(String version, String displayname) {
        this.version = version;
        this.displayname = displayname;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDisplayName() {
        return this.displayname;
    }


    @Override
    public String toString() {
        return this.displayname;
    }
}