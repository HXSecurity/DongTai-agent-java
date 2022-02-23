package io.dongtai.iast.agent.middlewarerecognition.tomcat;

import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class TomcatV7 extends AbstractTomcat {

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        return isMatch(paramRuntimeMXBean, loader, TomcatVersion.V7);
    }

    @Override
    public String getName() {
        return TomcatVersion.V7.getDisplayName();
    }

    @Override
    public String getVersion() {
        return TomcatVersion.V7.getVersion();
    }
}
