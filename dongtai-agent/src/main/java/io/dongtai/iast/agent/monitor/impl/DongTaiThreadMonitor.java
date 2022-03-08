package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class DongTaiThreadMonitor implements IMonitor {
    private final String name = "DongTaiThreadMonitor";


    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }

    @Override
    public void check() {
        try {
            JSONObject report = new JSONObject();
            JSONObject detail = new JSONObject();
            Set<String> dongTaiThreads = ThreadUtils.getDongTaiThreads();
            Set<String> notExistThreads = new HashSet<String>();
            for (IMonitor monitor : MonitorDaemonThread.monitorTasks) {
                if (!dongTaiThreads.contains(monitor.getName())) {
                    notExistThreads.add(monitor.getName());
                }
            }
            // 上传洞态监控线程不存在的信息
            if (notExistThreads.size() > 0 ) {
                detail.put(Constant.KEY_NOT_EXIST_THREADS, notExistThreads.toString());
                report.put(Constant.KEY_UPDATE_REPORT, Constant.REPORT_ERROR_THREAD);
                report.put(Constant.KEY_REPORT_VALUE, detail);
                HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD,report.toString());
            }
        }catch (Throwable t){
            DongTaiLog.warn("Report error thread failed, msg:{}, err:{}",t.getMessage(),t.getCause());
        }
    }


    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            this.check();
            ThreadUtils.threadSleep(60);
        }
    }

}