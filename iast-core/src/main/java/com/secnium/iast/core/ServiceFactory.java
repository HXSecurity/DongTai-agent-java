package com.secnium.iast.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.report.HeartBeatReport;
import com.secnium.iast.core.report.VulnReport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceFactory {
    private static final long DELAY = 60000;
    private static final long HEARTBEAT_MIN_DELAY = 10000;
    private static final long VULNREPORT_MIN_DELAY = 100;
    private static ServiceFactory INSTANCE;
    private final long heartbeatMisc;
    private final long reportTims;
    private final ScheduledExecutorService executorService;

    HeartBeatReport heartBeat = null;
    VulnReport report = null;
    HttpRequestReplay requestReplay = new HttpRequestReplay();

    public static ServiceFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ServiceFactory();
        }
        return INSTANCE;
    }

    public ServiceFactory() {
        assert null != PropertyUtils.getInstance();
        PropertyUtils propertiesUtils = PropertyUtils.getInstance();
        this.heartbeatMisc = propertiesUtils.getHeartBeatInterval();
        this.reportTims = propertiesUtils.getReportInterval();
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-engine-report").build());
    }

    public void init() {
        heartBeat = new HeartBeatReport(Math.max(heartbeatMisc, HEARTBEAT_MIN_DELAY));
        report = new VulnReport(Math.max(reportTims, VULNREPORT_MIN_DELAY));
    }

    public void start() {
        executorService.scheduleWithFixedDelay(heartBeat, DELAY, heartbeatMisc, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(report, 5000, reportTims, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(requestReplay, 5000, heartbeatMisc, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        // todo: 考虑是否需要挂起线程
    }

    public void destory() {
    }
}
