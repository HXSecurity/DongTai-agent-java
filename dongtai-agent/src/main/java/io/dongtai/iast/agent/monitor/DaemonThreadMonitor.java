package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class DaemonThreadMonitor implements IMonitor {
    private final String name = "ThreadMonitor";
    private int maxThreadNums = 10; // 最大线程数量，可以根据配置文件读取，也可以走服务端下发。


    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }

    @Override
    public void check() {
        try {
            JSONObject report = new JSONObject();
            Set<String> dongTaiThreads = getDongTaiThreads();
            JSONObject detail = new JSONObject();
            Set<String> errorThreads = new HashSet<String>();
            for (IMonitor monitor : MonitorDaemonThread.monitorTasks) {
                if (!dongTaiThreads.contains(monitor.getName())) {
                    errorThreads.add(monitor.getName());
                }
            }
            if (errorThreads.size() > 0 || dongTaiThreads.size() > maxThreadNums) {
                detail.put("ErrorThread", errorThreads.toString());
                detail.put("DongTaiThreadsNum", dongTaiThreads.size());
                detail.put("DongTaiThreads", dongTaiThreads.toString());
                report.put(Constant.KEY_UPDATE_REPORT, Constant.REPORT_ERROR_THREAD);
                report.put(Constant.KEY_REPORT_VALUE, detail);
                HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD,report.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            DongTaiLog.info("Thread Monitor Check");
            this.check();
            MonitorDaemonThread.threadSleep();
        }
    }

    private Set<String> getDongTaiThreads(){
        Set<String> threadSet = new HashSet<String>();
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int threadNum = currentGroup.activeCount();
        Thread[] threads = new Thread[threadNum];
        currentGroup.enumerate(threads);
        for (int i = 0; i < threadNum; i++) {
            // 匹配DongTai线程
            if(threads[i].getName().contains("DongTai")){
                threadSet.add(threads[i].getName());
            }
        }
        return threadSet;
    }
}
