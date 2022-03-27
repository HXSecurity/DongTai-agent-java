package com.secnium.iast.core;

import io.dongtai.iast.core.bytecode.IastClassFileTransformer;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.init.impl.ConfigEngine;
import io.dongtai.iast.core.init.impl.TransformEngine;
import io.dongtai.iast.core.service.StartUpTimeReport;
import io.dongtai.iast.core.utils.Constants;
import io.dongtai.log.DongTaiLog;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.PropertyUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentEngine {

    private static AgentEngine instance;

    ArrayList<IEngine> engines = new ArrayList<IEngine>();

    private static AgentEngine getInstance() {
        if (instance == null) {
            instance = new AgentEngine();
        }
        return instance;
    }

    public AgentEngine() {
        engines.add(new ConfigEngine());
        engines.add(new TransformEngine());
    }


    public static void install(String mode, String propertiesFilePath, Integer agentId, Instrumentation inst,
                               String agentFile) {
        if ("true".equals(System.getProperty("DongTai.IAST.Status"))) {
            DongTaiLog.info("DongTai IAST has Installed.");
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        DongTaiLog.info("DongTai Engine is about to be installed, the installation mode is {}", mode);
        PropertyUtils cfg = PropertyUtils.getInstance(propertiesFilePath);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("DongTai UncaughtExceptionHandler");
                DongTaiLog.error(e);
            }
        });
        EngineManager.getInstance(agentId);
        AgentEngine agentEngine = AgentEngine.getInstance();
        agentEngine.init(mode, cfg, inst);
        // Time consuming location
        agentEngine.run();
        System.setProperty("DongTai.IAST.Status", "true");

        stopWatch.stop();
        StartUpTimeReport.sendReport(EngineManager.getAgentId(), (int) stopWatch.getTime());
        IastClassFileTransformer transformer = IastClassFileTransformer.getInstance(inst);
        DongTaiLog.info("DongTai Engine is successfully installed to the JVM, and it takes {} s",
                stopWatch.getTime() / 1000);
        DongTaiLog.info("DongTai Agent Version: {}, DongTai Server: {}", Constants.AGENT_VERSION_VALUE, cfg.getBaseUrl());
    }

    public static void start() {
        DongTaiLog.info("Turn on the engine");
        EngineManager.turnOnEngine();
        DongTaiLog.info("Engine opened successfully");
    }

    public static void stop() {
        DongTaiLog.info("Turn off the engine");
        EngineManager.turnOffEngine();
        DongTaiLog.info("Engine shut down successfully");
    }

    public static void destroy(String mode, String propertiesFilePath, Instrumentation inst) {
        DongTaiLog.info("Uninstall engine");
        AgentEngine agentEngine = AgentEngine.getInstance();
        assert agentEngine != null;
        agentEngine.destroy();
        System.clearProperty("DongTai.IAST.Status");
        DongTaiLog.info("Engine uninstallation succeeded");
    }


    /**
     * // 初始化引擎
     */
    public void init(String mode, PropertyUtils propertiesUtils, Instrumentation inst) {
        for (IEngine engine : engines) {
            engine.init(propertiesUtils, inst);
        }
    }

    /**
     * 启动引擎
     */
    private void run() {
        for (IEngine engine : engines) {
            engine.start();
        }
    }

    /**
     * 销毁引擎
     */
    private void destroy() {
        ListIterator<IEngine> listIterator = engines.listIterator(engines.size());
        IEngine engine;
        while (listIterator.hasPrevious()) {
            engine = listIterator.previous();
            engine.destroy();
        }
    }

}
