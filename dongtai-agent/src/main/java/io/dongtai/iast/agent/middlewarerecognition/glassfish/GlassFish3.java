package io.dongtai.iast.agent.middlewarerecognition.glassfish;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;
import java.net.URL;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GlassFish3 implements IServer {
    public static final String HTTP_SERVER = " org/glassfish/grizzly/http/server/HttpServer.class".substring(1);

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        try {
            URL url = loader.getResource(HTTP_SERVER);
            return (url != null);
        } catch (Throwable ignoreable) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "grizzly";
    }

    @Override
    public String getVersion() {
        return "Grizzly";
    }
}
