package com.secnium.iast.core;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.state.State;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.init.impl.ConfigEngine;
import io.dongtai.iast.core.init.impl.TransformEngine;
import io.dongtai.iast.core.service.*;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.dongtai.SpyDispatcherHandler;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentEngine {

    private static AgentEngine instance;
    private PolicyManager policyManager;
    private static final AgentState AGENT_STATE = AgentState.getInstance();

    ArrayList<IEngine> engines = new ArrayList<IEngine>();

    public static AgentEngine getInstance() {
        if (instance == null) {
            instance = new AgentEngine();
        }
        return instance;
    }

    public AgentEngine() {
        engines.add(new ConfigEngine());
        engines.add(new TransformEngine());
    }

    public PolicyManager getPolicyManager() {
        return this.policyManager;
    }

    public void setPolicyManager(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }

    public static void install(String mode, String propertiesFilePath, Integer agentId, Instrumentation inst,
                               String agentFile) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            DongTaiLog.debug("DongTai Engine is about to be installed, the installation mode is {}", mode);
            PropertyUtils cfg = PropertyUtils.getInstance(propertiesFilePath);
            EngineManager.getInstance(agentId);
            PolicyManager policyManager = new PolicyManager();
            AgentEngine agentEngine = AgentEngine.getInstance();
            agentEngine.setPolicyManager(policyManager);

            agentEngine.init(mode, cfg, inst, policyManager);
            // Time-consuming location
            agentEngine.run();

            stopWatch.stop();
            StartUpTimeReport.sendReport(EngineManager.getAgentId(), (int) stopWatch.getTime());
            DongTaiLog.info("DongTai Engine is successfully installed to the JVM, and it takes {} s",
                    stopWatch.getTime() / 1000);
            DongTaiLog.info("DongTai Agent Version: {}, DongTai Server: {}", AgentConstant.VERSION_VALUE, cfg.getBaseUrl());
            new ServiceDirReport().send();
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("ENGINE_INSTALL_FAILED"), e);
            AGENT_STATE.setState(State.EXCEPTION);
        }
    }

    public static void start() {
        DongTaiLog.info("Turn on the engine successfully");
    }

    public static void stop() {
        DongTaiLog.info("Turn off the engine successfully");
    }

    public static void destroy(String mode, String propertiesFilePath, Instrumentation inst) {
        try {
            DongTaiLog.info("Uninstall engine");
            AgentEngine agentEngine = AgentEngine.getInstance();
            assert agentEngine != null;
            agentEngine.destroy();
            ThreadPools.destroy();
            ServiceFactory.getInstance().destroy();
            SpyDispatcherHandler.destroy();
            DongTaiLog.info("Engine uninstallation succeeded");
            EngineManager.cleanThreadState();
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("ENGINE_DESTROY_FAILED"), e);
            AGENT_STATE.setState(State.EXCEPTION);
        }
    }


    /**
     * 初始化引擎
     */
    public void init(String mode, PropertyUtils propertiesUtils, Instrumentation inst, PolicyManager policyManager) {
        for (IEngine engine : engines) {
            engine.init(propertiesUtils, inst, policyManager);
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
