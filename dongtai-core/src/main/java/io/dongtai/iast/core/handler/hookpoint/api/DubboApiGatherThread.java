package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.DubboImpl;
import io.dongtai.log.DongTaiLog;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class DubboApiGatherThread extends AbstractApiGatherThread {

    public static final String FRAMEWORK_NAME = "dubbo";

    // Dubbo的包名是alibaba还是apache的
    public static enum DubboPackage {
        ALIBABA,
        APACHE;
    }

    // avoid lock
    private static boolean isStarted = false;

    public static void gather(Class handlerClass) {
        if (isStarted) {
            return;
        }
        isStarted = true;

        DubboPackage dubboPackage = parseDubboPackage(handlerClass);
        if (dubboPackage == null) {
            return;
        }
        new DubboApiGatherThread(dubboPackage).start();
    }

    /**
     * 根据类名解析当前是dubbo包还是alibaba的包
     *
     * @param clazz
     * @return
     */
    private static DubboPackage parseDubboPackage(Class clazz) {

        if (clazz == null) {
            DongTaiLog.error("DubboApiGatherThread.parseDubboPackage class is null");
            return null;
        }

        if (clazz.getName().startsWith(" org.apache.".substring(1))) {
            return DubboPackage.APACHE;
        } else if (clazz.getName().startsWith(" com.alibaba.".substring(1))) {
            return DubboPackage.ALIBABA;
        } else {
            DongTaiLog.error("DubboApiGatherThread.parseDubboPackage can not parse dubbo package for class {}", clazz.getName());
            return null;
        }
    }

    private final DubboPackage dubboPackage;

    public DubboApiGatherThread(DubboPackage dubboPackage) {
        super(AgentConstant.THREAD_NAME_PREFIX_CORE + "DubboGatherApi-thread");
        this.dubboPackage = dubboPackage;
    }

    @Override
    public void run() {
        switch (this.dubboPackage) {
            case ALIBABA:
                this.gatherAlibabaDubboService();
                break;
            case APACHE:
                this.gatherApacheDubboService();
                break;
            default:
                // fuck
        }
    }

    /**
     * 收集Alibaba Dubbo的Service
     */
    private void gatherAlibabaDubboService() {
        try {
            Class<?> proxyClass = DubboImpl.getClassLoader().loadClass("io.dongtai.iast.api.gather.dubbo.extractor.AlibabaDubboServiceExtractor");
            Object openApi = proxyClass.getDeclaredMethod("run").invoke(null);
            report(openApi, FRAMEWORK_NAME);
        } catch (NoClassDefFoundError e) {
            DongTaiLog.error("DubboApiGatherThread.gatherAlibabaDubbo NoClassDefFoundError", e);
        } catch (Throwable e) {
            DongTaiLog.error("DubboApiGatherThread.gatherAlibabaDubbo error", e);
        }
    }

    /**
     * 收集Apache Dubbo的Service
     */
    private void gatherApacheDubboService() {
        try {
            Class<?> proxyClass = DubboImpl.getClassLoader().loadClass("io.dongtai.iast.api.gather.dubbo.extractor.ApacheDubboServiceExtractor");
            Object openApi = proxyClass.getDeclaredMethod("run").invoke(null);
            report(openApi, FRAMEWORK_NAME);
        } catch (NoClassDefFoundError e) {
            DongTaiLog.error("DubboApiGatherThread.gatherApacheDubbo NoClassDefFoundError", e);
        } catch (Throwable e) {
            DongTaiLog.error("DubboApiGatherThread.gatherApacheDubbo error", e);
        }
    }

}
