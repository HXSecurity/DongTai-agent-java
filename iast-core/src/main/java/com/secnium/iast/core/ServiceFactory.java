package com.secnium.iast.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.report.AgentQueueReport;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceFactory {

    private static ServiceFactory INSTANCE;
    private final long replayInterval;
    private final ScheduledExecutorService queueService;
    private final ScheduledExecutorService replayService;

    HttpRequestReplay requestReplay = null;
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
        this.queueService = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-queue").build());
        this.replayService = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("dongtai-replay").build());
    }

    public void init() {
        agentQueueSender = new AgentQueueReport();
        requestReplay = new HttpRequestReplay();
    }

    public void start() {
        queueService.scheduleWithFixedDelay(agentQueueSender, 0, PropertyUtils.getInstance().getHeartBeatInterval(),
                TimeUnit.SECONDS);
        replayService.scheduleWithFixedDelay(requestReplay, 0, replayInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        // todo: 考虑是否需要挂起线程
    }

    public void destory() {
    }
}
