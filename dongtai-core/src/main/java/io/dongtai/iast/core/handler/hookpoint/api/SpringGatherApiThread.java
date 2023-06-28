package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class SpringGatherApiThread extends AbstractApiGatherThread {

    public static final String FRAMEWORK_NAME = "spring mvc";

    // avoid lock
    private static volatile boolean isStarted = false;

    public static void gather(Object applicationContext) {
        if (isStarted) {
            return;
        }
        isStarted = true;
        new SpringGatherApiThread(applicationContext).start();
    }

    private final Object applicationContext;

    public SpringGatherApiThread(Object applicationContext) {
        // TODO 2023-6-28 11:38:25
        super(AgentConstant.THREAD_NAME_PREFIX_CORE + "SpringMvcGatherApi-thread");
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            Class<?> proxyClass = HttpImpl.getClassLoader().loadClass("io.dongtai.iast.spring.gather.SpringMVCApiGather");
            Method getAPI = proxyClass.getDeclaredMethod("gather", Object.class);
            Object openApi = getAPI.invoke(null, applicationContext);
            report(openApi, FRAMEWORK_NAME);
        } catch (NoClassDefFoundError e) {
            DongTaiLog.debug("SpringGatherApiThread NoClassDefFoundError ", e);
        } catch (Throwable e) {
            DongTaiLog.error("SpringGatherApiThread.reflection failed", e);
        }
    }

}