package com.secnium.iast.agent.middlewarerecognition.tomcat;

import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public final class TomcatV9 extends AbstractTomcat {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        return isMatch(paramRuntimeMXBean, TomcatVersion.V9);
    }

    @Override
    public String getName() {
        return TomcatVersion.V9.getDisplayName();
    }

    @Override
    public String getVersion() {
        return TomcatVersion.V9.getVersion();
    }
}

