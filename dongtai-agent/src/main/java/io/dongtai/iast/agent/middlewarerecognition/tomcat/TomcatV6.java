package io.dongtai.iast.agent.middlewarerecognition.tomcat;


import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class TomcatV6 extends AbstractTomcat {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        return isMatch(paramRuntimeMXBean, loader, TomcatVersion.V6);
    }

    @Override
    public String getName() {
        return TomcatVersion.V6.getDisplayName();
    }

    @Override
    public String getVersion() {
        return TomcatVersion.V6.getVersion();
    }
}