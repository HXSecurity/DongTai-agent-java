package com.secnium.iast.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.engines.impl.*;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.NamespaceConvert;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentEngine {

    private static AgentEngine instance;

    public static LoggerContext DEFAULT_LOGGERCONTEXT = new LoggerContext();
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
        //engines.add(new LoggerEngine());
        engines.add(new ConfigEngine());
        engines.add(new SandboxEngine());
        engines.add(new ServiceEngine());
        engines.add(new SpyEngine());
        engines.add(new TransformEngine());
    }

    public static void install(String mode, String propertiesFilePath, Instrumentation inst) {
        long start = System.currentTimeMillis();
        System.out.println("[com.dongtai.engine] The engine is about to be installed, the installation mode is " + mode);
        configureLogback();
        Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(AgentEngine.class);
        logger.info("Log module initialized successfully");
        AgentEngine agentEngine = AgentEngine.getInstance();
        assert agentEngine != null;
        agentEngine.setInst(inst);
        agentEngine.init(mode, propertiesFilePath, inst);
        agentEngine.run();

        long total = System.currentTimeMillis() - start;
        System.out.println("[com.dongtai.engine] The engine is successfully installed to the JVM, and it takes " + total + "ms");
        AgentRegisterReport.send();
    }

    public static void start() {
        System.out.println("[com.dongtai.engine] Turn on the engine");
        EngineManager.turnOnEngine();
        System.out.println("[com.dongtai.engine] Engine opened successfully");
    }

    public static void stop() {
        System.out.println("[com.dongtai.engine] Turn off the engine");
        EngineManager.turnOffEngine();
        System.out.println("[com.dongtai.engine] Engine shut down successfully");
    }

    public static void destroy(String mode, String propertiesFilePath, Instrumentation inst) {
        System.out.println("[com.dongtai.engine] Uninstall engine");
        AgentEngine agentEngine = AgentEngine.getInstance();
        assert agentEngine != null;
        agentEngine.destroy();
        System.out.println("[com.dongtai.engine] Engine uninstallation succeeded");
    }


    /**
     * // 初始化引擎
     */
    public void init(String mode, String propertiesFilePath, Instrumentation inst) {
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);


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

    /**
     * 覆盖logback默认的配置机制
     */
    private static void configureLogback() {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(DEFAULT_LOGGERCONTEXT);
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = AgentEngine.class.getClassLoader().getResourceAsStream("logback.xml");
            configurator.doConfigure(configStream);
        } catch (JoranException e) {
            e.printStackTrace();
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
