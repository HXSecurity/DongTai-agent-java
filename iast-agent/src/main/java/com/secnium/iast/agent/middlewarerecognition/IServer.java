package com.secnium.iast.agent.middlewarerecognition;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface IServer {
    boolean isMatch(RuntimeMXBean paramRuntimeMXBean);

    String getName();

    String getVersion();
}
