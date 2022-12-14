package io.dongtai.iast.agent.fallback;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.common.state.State;
import io.dongtai.log.DongTaiLog;

/**
 * 降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class FallbackSwitch {

    private FallbackSwitch() {
        throw new IllegalStateException("Utility class");
    }

    public static void setPerformanceFallback(State state) {
        if (EngineManager.getFallbackManager() == null) {
            return;
        }
        EngineManager engineManager = EngineManager.getInstance();

        if (engineManager.getAgentState().isException()) {
            DongTaiLog.warn("performance fallback state change to {}, but agent currently has exception", state.name());
            return;
        }
        DongTaiLog.info("performance fallback state change to {}", state.name());

        if (state == State.UNINSTALLED) {
            if (!engineManager.getAgentState().isUninstalled()) {
                engineManager.uninstall();
                engineManager.getAgentState().fallbackToUninstall();
            }
        } else {
            if (engineManager.getAgentState().isUninstalled()) {
                engineManager.install();
            }

            if (state == State.RUNNING && !engineManager.getAgentState().isRunning()) {
                engineManager.getAgentState().fallbackRecover();
            } else if (state == State.PAUSED && !engineManager.getAgentState().isPaused()) {
                engineManager.getAgentState().fallbackToPause();
            }
        }
    }
}
