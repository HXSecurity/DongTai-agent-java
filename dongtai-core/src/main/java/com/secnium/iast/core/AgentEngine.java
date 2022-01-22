package com.secnium.iast.core;

import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.engines.impl.ConfigEngine;
import com.secnium.iast.core.engines.impl.ServiceEngine;
import com.secnium.iast.core.engines.impl.TransformEngine;
import com.secnium.iast.core.report.StartUpTimeReport;
import com.secnium.iast.log.DongTaiLog;
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
        engines.add(new ServiceEngine());
        engines.add(new TransformEngine());
    }


    public static void install(String mode, String propertiesFilePath, Integer agentId, Instrumentation inst,
            String agentFile) {
        if ("true".equals(System.getProperty("DongTai.IAST.Status"))) {
            DongTaiLog.info("DongTai IAST has Installed.");
            return;
        }
        long start = System.currentTimeMillis();

        DongTaiLog.info("DongTai Engine is about to be installed, the installation mode is {}", mode);
        PropertyUtils cfg = PropertyUtils.getInstance(propertiesFilePath);
        EngineManager.getInstance(agentId);
        AgentEngine agentEngine = AgentEngine.getInstance();
        agentEngine.init(mode, cfg, inst);
        agentEngine.run();
        System.setProperty("DongTai.IAST.Status", "true");

        long startupTime = System.currentTimeMillis() - start;
        StartUpTimeReport.sendReport(EngineManager.getAgentId(), (int) startupTime);
        EngineManager.agentStarted();
        DongTaiLog.info("DongTai Engine is successfully installed to the JVM, and it takes {} s",
                startupTime / 1000);
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
