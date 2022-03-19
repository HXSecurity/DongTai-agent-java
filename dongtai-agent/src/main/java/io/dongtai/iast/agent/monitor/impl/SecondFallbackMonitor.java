package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.log.DongTaiLog;

/**
 * 二次降级(高频流量熔断器、性能熔断器)监控器
 *
 * @author liyuan40
 * @date 2022/3/7 20:03
 */
public class SecondFallbackMonitor implements IMonitor {

    private static final String NAME = "SecondFallbackMonitor";

    private final EngineManager engineManager;

    public SecondFallbackMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public void check() throws Exception {
        final Class<?> fallbackManagerClass = EngineManager.getFallbackManagerClass();
        if (fallbackManagerClass == null) {
            return;
        }
        // 检查是否需要二次降级
        Boolean isNeedSecondFallback = (Boolean) fallbackManagerClass.getMethod("isNeedSecondFallback").invoke(null);
        if (isNeedSecondFallback) {
            DongTaiLog.info("SecondFallbackCheck result is true, ready to execute second fallback operation.");
            engineManager.uninstall();
        }
    }

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + NAME;
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            try {
                this.check();
            } catch (Throwable t) {
                DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
            }
            ThreadUtils.threadSleep(60);
        }
    }
}
