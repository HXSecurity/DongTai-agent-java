package io.dongtai.iast.agent.middlewarerecognition.jboss;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * JBoss中间件从JBoss7开始，更名为JBoss AS 7，在版本8又更名为WildFly 8
 *
 * @author dongzhiyong@huoxian.cn
 */
public class JBossAS implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        return runtimeMXBean.getSystemProperties().get("jboss.server.base.dir") != null;
    }

    @Override
    public String getName() {
        return "JBossAS";
    }

    @Override
    public String getVersion() {
        return "7 or later";
    }
}
