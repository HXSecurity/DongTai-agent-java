package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.bytecode.enhance.plugin.PluginRegister;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.DubboImpl;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class DubboApiGatherThread extends AbstractApiGatherThread {

    public static final String FRAMEWORK_NAME = "dubbo";

    // dubbo api采集插件的名字，可以在启动agent的时候通过指定属性禁用它
    public static final String PLUGIN_NAME = "dubbo-api";

    // Dubbo的包名是alibaba还是apache的
    public enum DubboPackage {
        ALIBABA,
        APACHE
    }

    // avoid lock
    private static boolean isStarted = false;

    public static void gather(Class handlerClass) {
        if (isStarted) {
            return;
        }
        isStarted = true;

        // 判断插件是否开启，仅当开启的情况下才采集
        if (PluginRegister.isPluginDisable(PLUGIN_NAME)) {
            DongTaiLog.debug("dubbo api gather plugin disable");
            return;
        }

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
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.API_GATHER_DUBBO_ALIBABA_ERROR, e);
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
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.API_GATHER_DUBBO_APACHE_ERROR, e);
        }
    }

}
