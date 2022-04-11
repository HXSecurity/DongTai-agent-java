package io.dongtai.iast.agent.middlewarerecognition.websphere;


import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.iast.agent.middlewarerecognition.PackageManager;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class WebSphere implements IServer {
    static final String PACKAGE_NAME = "bootstrap";
    static final String WS90 = "WAS90.";
    static final String WS85 = "WAS855.";
    static final String IBM_FLAG = "IBM Corp.";
    private static final String WS_CLASS = " com.ibm.ws.bootstrap.WSLauncher".substring(1);

    static WebSphereVersion recognize() {
        Package wspachage = (new PackageManager(WS_CLASS)).getPackage();
        return parseVersion(wspachage);
    }

    static WebSphereVersion parseVersion(Package paramPackage) {
        if (paramPackage == null) {
            return null;
        }
        if (isMatch(paramPackage)) {
            return getVersion(paramPackage.getImplementationVersion());
        }
        return null;
    }

    private static WebSphereVersion getVersion(String paramString) {
        if (paramString != null) {
            if (paramString.startsWith(WS90)) {
                return WebSphereVersion.v9;
            }
            if (paramString.startsWith(WS85)) {
                return WebSphereVersion.v85;
            }
        }
        return null;
    }


    private static boolean isMatch(Package paramPackage) {
        return (PACKAGE_NAME.equals(paramPackage.getImplementationTitle()) &&
                IBM_FLAG.equals(paramPackage.getImplementationVendor()));
    }

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        WebSphereVersion webSphereVersion = WebSphere.recognize();
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }
}
