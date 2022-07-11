package io.dongtai.iast.agent;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;

public class ShutdownThread extends Thread {

    @Override
    public void run() {
        if (!MonitorDaemonThread.isExit){
            EngineManager.getInstance().uninstall();
        }
    }

}
