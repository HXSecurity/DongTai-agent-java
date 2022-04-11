package io.dongtai.iast.agent.middlewarerecognition.weblogic;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.io.File;
import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class WebLogic implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        File runFile = new File(".", "bin/startWebLogic.sh");
        File configFile = new File(".", "init-info/domain-info.xml");
        System.setProperty("UseSunHttpHandler", "true");
        return runFile.exists() && configFile.exists();
    }

    @Override
    public String getName() {
        return "WebLogic";
    }

    @Override
    public String getVersion() {
        // 从xml中解析版本
        return "WebLogic";
    }
}
