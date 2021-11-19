package com.secnium.iast.agent;

import com.secnium.iast.agent.monitor.MonitorDaemonThread;
import com.secnium.iast.agent.util.LogUtils;

public class ShutdownThread extends Thread {

    @Override
    public void run() {
        LogUtils.info("enter shutdown thread");
        MonitorDaemonThread.isExit = true;
        LogUtils.info("exit shutdown thread");
    }

}
