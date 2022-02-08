package com.secnium.iast.agent.middlewarerecognition.tomcat;


import java.lang.management.RuntimeMXBean;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class TomcatV8 extends AbstractTomcat {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        return isMatch(paramRuntimeMXBean, TomcatVersion.V8);
    }

    @Override
    public String getName() {
        return TomcatVersion.V8.getDisplayName();
    }

    @Override
    public String getVersion() {
        return TomcatVersion.V8.getVersion();
    }
}
