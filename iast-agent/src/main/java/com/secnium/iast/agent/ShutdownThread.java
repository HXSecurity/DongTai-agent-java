package com.secnium.iast.agent;

import com.secnium.iast.agent.monitor.MonitorDaemonThread;

public class ShutdownThread extends Thread {

    @Override
    public void run() {
        System.out.println("enter shutdown thread");
        MonitorDaemonThread.isExit = true;
        System.out.println("exit shutdown thread");
    }

}
