package com.secnium.iast.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.secnium.iast.core.replay.HttpRequestReplay;
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
    private final ScheduledExecutorService executorService;

    ReportSender report = null;
    HttpRequestReplay requestReplay = null;
    MethodReportSender methodReportSender = null;

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
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-engine-report").build());
    }

    public void init() {
        methodReportSender = new MethodReportSender();
        report = new ReportSender();
        requestReplay = new HttpRequestReplay();
    }

    public void start() {
        executorService.scheduleWithFixedDelay(methodReportSender, 0, reportInterval, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(report, 0, reportInterval, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(requestReplay, 0, replayInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        // todo: 考虑是否需要挂起线程
    }

    public void destory() {
    }
}
