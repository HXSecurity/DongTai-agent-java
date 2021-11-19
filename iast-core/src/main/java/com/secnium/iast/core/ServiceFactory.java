package com.secnium.iast.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.report.AgentQueueReport;
import com.secnium.iast.core.report.MethodReportSender;
import com.secnium.iast.core.report.ReportSender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceFactory {
    private static ServiceFactory INSTANCE;
    private final long replayInterval;
    private final long reportInterval;
    private final ScheduledExecutorService queueService;
    private final ScheduledExecutorService reportService;
    private final ScheduledExecutorService methodService;
    private final ScheduledExecutorService replayService;

    ReportSender report = null;
    HttpRequestReplay requestReplay = null;
    MethodReportSender methodReportSender = null;
    AgentQueueReport agentQueueSender = null;

    public static ServiceFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ServiceFactory();
        }
        return INSTANCE;
    }

    /**
     * fixme 优化线程池创建
     */
    public ServiceFactory() {
        assert null != PropertyUtils.getInstance();
        PropertyUtils propertiesUtils = PropertyUtils.getInstance();
        this.replayInterval = propertiesUtils.getReplayInterval();
        this.reportInterval = propertiesUtils.getReportInterval();
        this.queueService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-queue").build());
        this.reportService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-report").build());
        this.methodService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-method").build());
        this.replayService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-replay").build());
    }

    public void init() {
        agentQueueSender = new AgentQueueReport();
        methodReportSender = new MethodReportSender();
        report = new ReportSender();
        requestReplay = new HttpRequestReplay();
    }

    public void start() {
        queueService.scheduleWithFixedDelay(agentQueueSender,0,PropertyUtils.getInstance().getHeartBeatInterval(),TimeUnit.SECONDS);
        methodService.scheduleWithFixedDelay(methodReportSender, 0, reportInterval, TimeUnit.MILLISECONDS);
        reportService.scheduleWithFixedDelay(report, 0, reportInterval, TimeUnit.MILLISECONDS);
        replayService.scheduleWithFixedDelay(requestReplay, 0, replayInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        // todo: 考虑是否需要挂起线程
    }

    public void destory() {
    }
}
