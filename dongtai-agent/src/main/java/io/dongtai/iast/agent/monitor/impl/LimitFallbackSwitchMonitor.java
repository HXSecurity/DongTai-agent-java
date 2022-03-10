package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.log.DongTaiLog;

/**
 * 限制降级开关(高频流量熔断器、性能熔断器)监控器
 *
 * @author liyuan40
 * @date 2022/3/7 20:03
 */
public class LimitFallbackSwitchMonitor implements IMonitor {

    private final String name = "LimitFallbackSwitchMonitor";

    private final EngineManager engineManager;

    public LimitFallbackSwitchMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public void check() {
        Boolean isNeedSecondFallback = checkIsNeedSecondFallback();
        if (isNeedSecondFallback == null) {
            return;
        }

        if (isNeedSecondFallback) {
            engineManager.uninstall();
        }
    }

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }

    /**
     * 检查是否需要二次降级
     *
     * @return boolean
     */
    private Boolean checkIsNeedSecondFallback() {
        try {
            final Class<?> limitFallbackSwitch = EngineManager.getLimitFallbackSwitch();
            if (limitFallbackSwitch == null) {
                return null;
            }
            return (Boolean) limitFallbackSwitch.getMethod("isNeedSecondFallback").invoke(null);
        } catch (Throwable t) {
            DongTaiLog.error("checkIsNeedSecondFallback failed, msg:{}, err:{}", t.getMessage(), t.getCause());
            return false;
        }
    }

    @Override
    public void run() {
        while(!MonitorDaemonThread.isExit) {
            this.check();
            ThreadUtils.threadSleep(60);
        }
    }
}
