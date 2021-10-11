package com.secnium.iast.core;

import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.engines.impl.*;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentEngine {

    private static Logger logger = LogUtils.getLogger(AgentEngine.class);
    private static AgentEngine instance;

    public Instrumentation getInst() {
        return inst;
    }

    public void setInst(Instrumentation inst) {
        this.inst = inst;
    }

    private Instrumentation inst;
    ArrayList<IEngine> engines = new ArrayList<IEngine>();

    private static AgentEngine getInstance() {
        if (instance == null) {
            instance = new AgentEngine();
        }
        return instance;
    }

    public AgentEngine() {
        engines.add(new ConfigEngine());
        engines.add(new SandboxEngine());
        engines.add(new ServiceEngine());
        engines.add(new SpyEngine());
        engines.add(new TransformEngine());
    }


    public static void install(String mode, String propertiesFilePath, Instrumentation inst) {
        long start = System.currentTimeMillis();
        logger.info("The engine is about to be installed, the installation mode is {}", mode);
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);
        AgentRegisterReport.send();

        AgentEngine agentEngine = AgentEngine.getInstance();
        assert agentEngine != null;
        agentEngine.setInst(inst);
        agentEngine.init(mode, propertiesUtils, inst);
        agentEngine.run();

        logger.info("The engine is successfully installed to the JVM, and it takes {} s", (System.currentTimeMillis() - start) / 1000);
    }

    public static void start() {
        logger.info("Turn on the engine");
        EngineManager.turnOnEngine();
        logger.info("Engine opened successfully");
    }

    public static void stop() {
        logger.info("Turn off the engine");
        EngineManager.turnOffEngine();
        logger.info("Engine shut down successfully");
    }

    public static void destroy(String mode, String propertiesFilePath, Instrumentation inst) {
        logger.info("Uninstall engine");
        AgentEngine agentEngine = AgentEngine.getInstance();
        assert agentEngine != null;
        agentEngine.destroy();
        logger.info("Engine uninstallation succeeded");
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
