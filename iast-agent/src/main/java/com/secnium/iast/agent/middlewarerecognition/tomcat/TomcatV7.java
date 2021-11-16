package com.secnium.iast.agent.middlewarerecognition.tomcat;

import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class TomcatV7 extends AbstractTomcat {

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        return isMatch(paramRuntimeMXBean, TomcatVersion.V7);
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
