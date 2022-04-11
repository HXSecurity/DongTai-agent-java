package io.dongtai.iast.agent.middlewarerecognition.tomcat;

import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public final class TomcatV5 extends AbstractTomcat {

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        return isMatch(paramRuntimeMXBean, loader, TomcatVersion.V5);
    }


    @Override
    public String getName() {
        return TomcatVersion.V5.getDisplayName();
    }

    @Override
    public String getVersion() {
        return TomcatVersion.V5.getVersion();
    }
}

