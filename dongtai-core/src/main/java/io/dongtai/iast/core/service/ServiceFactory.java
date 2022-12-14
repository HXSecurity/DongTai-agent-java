package io.dongtai.iast.core.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.utils.PropertyUtils;

import java.util.concurrent.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceFactory {

    private static boolean RUNNING = false;
    private static ServiceFactory INSTANCE;
    private final ScheduledExecutorService queueService;

    AgentQueueReport agentQueueSender = null;

    public static ServiceFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ServiceFactory();
        }
        return INSTANCE;
    }

    public static void startService() {
        if (!ServiceFactory.RUNNING) {
            ServiceFactory.RUNNING = true;
            ServiceFactory.getInstance().start();
        }
    }

    public ServiceFactory() {
        this.queueService = Executors
                .newSingleThreadScheduledExecutor(
                        new ThreadFactoryBuilder().setNameFormat(AgentConstant.THREAD_NAME_PREFIX_CORE + "HeartBeat").build());
    }

    public void start() {
        agentQueueSender = new AgentQueueReport();
        queueService.scheduleWithFixedDelay(agentQueueSender, 0, PropertyUtils.getInstance().getHeartBeatInterval(),
                TimeUnit.SECONDS);
    }

    public void destroy(){
        INSTANCE = null;
        queueService.shutdown();
        agentQueueSender = null;
    }
}
