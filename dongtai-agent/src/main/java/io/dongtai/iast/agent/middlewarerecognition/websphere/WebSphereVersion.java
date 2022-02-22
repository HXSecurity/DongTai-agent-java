package io.dongtai.iast.agent.middlewarerecognition.websphere;

/**
 * @author dongzhiyong@huoxian.cn
 */

public enum WebSphereVersion {
    /**
     *
     */
    v6("websphere6.1", "WebSphere 6.1"),
    v7("websphere7", "WebSphere 7"),
    v85("websphere8.5", "WebSphere 8.5"),
    v9("websphere9", "WebSphere 9.0");

    private final String version;
    private final String displayName;

    WebSphereVersion(String version, String displayName) {
        this.version = version;
        this.displayName = displayName;
    }


    public String getVersion() {
        return this.version;
    }


    public String getDisplayName() {
        return this.displayName;
    }


    @Override
    public String toString() {
        return this.version;
    }
}
