package com.secnium.iast.core.report;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dongtai.log.DongTaiLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReportService {
    static class MyScheduledExecutor implements Runnable {

        private String jobName;

        MyScheduledExecutor(String jobName) {
            this.jobName = jobName;
        }

        @Override
        public void run() {

            System.out.println(jobName + " is running");
        }
    }

    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-engine-report").build());

        long initialDelay = 1;
        long period = 1000;
        // 从现在开始1秒钟之后，每隔1秒钟执行一次job1
//        service.scheduleAtFixedRate(new MyScheduledExecutor("job1"), initialDelay, period, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor("job2"), initialDelay, period * 2, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor("job3"), initialDelay, period * 3, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor("job4"), initialDelay, period * 4, TimeUnit.MILLISECONDS);


        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            DongTaiLog.error(e);
        }

        service.shutdown();
        System.out.println(service.isShutdown());
    }
}
