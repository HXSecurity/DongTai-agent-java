package com.secnium.iast.agent;

import com.secnium.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.log.DongTaiLog;

public class ShutdownThread extends Thread {

    @Override
    public void run() {
        DongTaiLog.info("enter shutdown thread");
        MonitorDaemonThread.isExit = true;
        DongTaiLog.info("exit shutdown thread");
    }

}
