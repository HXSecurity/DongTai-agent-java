package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class SpringGatherApiThread extends AbstractApiGatherThread {

    public static final String FRAMEWORK_NAME = "spring mvc";

    // avoid lock
    private static boolean isStarted = false;

    public static void gather(Object applicationContext) {
        if (isStarted) {
            return;
        }
        isStarted = true;

        new SpringGatherApiThread(applicationContext).start();
    }

    private final Object applicationContext;

    public SpringGatherApiThread(Object applicationContext) {
        super(AgentConstant.THREAD_NAME_PREFIX_CORE + "SpringMvcGatherApi-thread");
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            this.runWithClassLoader(HttpImpl.getClassLoader());
        } catch (NoClassDefFoundError e) {
            DongTaiLog.debug("SpringGatherApiThread NoClassDefFoundError ", e);

            // 让它继承当前线程的上下文，在Tomcat这种破坏双亲委派的场景作为fallback
            // 比如在Tomcat下可能Request是一个ClassLoader，可能Context中的Spring类被另一个单独的ParallelWebappClassLoader所加载
            try {
                IastClassLoader iastClassLoader = new IastClassLoader(
                        Thread.currentThread().getContextClassLoader(),
                        new URL[]{HttpImpl.IAST_REQUEST_JAR_PACKAGE.toURI().toURL()});
                this.runWithClassLoader(iastClassLoader);
            } catch (Throwable e2) {
                DongTaiLog.debug("SpringGatherApiThread NoClassDefFoundError 002", e2);
            }

        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.API_GATHER_SPRING_MVC_ERROR, e);
        }
    }

    /**
     * 使用给定的ClassLoader加载收集API
     *
     * @param classLoader
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private void runWithClassLoader(ClassLoader classLoader) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> proxyClass = classLoader.loadClass("io.dongtai.iast.api.gather.spring.extractor.SpringMVCApiExtractor");
        Method getAPI = proxyClass.getDeclaredMethod("run", Object.class);
        Object openApiList = getAPI.invoke(null, applicationContext);
        if (openApiList == null) {
            return;
        }
        // 返回的是一个报告列表
        ((List) openApiList).forEach(o -> report(o, FRAMEWORK_NAME));
    }

}