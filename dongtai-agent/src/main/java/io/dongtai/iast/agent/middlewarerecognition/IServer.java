package io.dongtai.iast.agent.middlewarerecognition;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface IServer {
    boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader);

    String getName();

    String getVersion();
}
