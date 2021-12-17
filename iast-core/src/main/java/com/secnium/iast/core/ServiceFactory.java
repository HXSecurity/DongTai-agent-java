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

    public ServiceFactory() {
        this.queueService = Executors
                .newSingleThreadScheduledExecutor(
                        new ThreadFactoryBuilder().setNameFormat("DongTai-HeartBeat").build());
        this.replayService = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("DongTai-Replay").build());
    }

    public void init() {
        agentQueueSender = new AgentQueueReport();
        requestReplay = new HttpRequestReplay();
    }

    public void start() {
        queueService.scheduleWithFixedDelay(agentQueueSender, 0, PropertyUtils.getInstance().getHeartBeatInterval(),
                TimeUnit.SECONDS);
        replayService.scheduleWithFixedDelay(requestReplay, 0, PropertyUtils.getInstance().getReplayInterval(),
                TimeUnit.MILLISECONDS);
    }

    public void stop() {
        // todo: 考虑是否需要挂起线程
    }

    public void destory() {
    }
}
