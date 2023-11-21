package io.dongtai.iast.core;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.Version;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.state.State;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.FastjsonCheck;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.QLExpressCheck;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.init.impl.ConfigEngine;
import io.dongtai.iast.core.init.impl.TransformEngine;
import io.dongtai.iast.core.service.ServiceDirReport;
import io.dongtai.iast.core.service.ServiceFactory;
import io.dongtai.iast.core.service.StartUpTimeReport;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.dongtai.SpyDispatcherHandler;
import java.lang.instrument.Instrumentation;
import java.util.*;

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
            DongTaiLog.info("DongTai Agent Version: {}, DongTai Server: {}", Version.VERSION, cfg.getBaseUrl());
            inject(inst);
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
            // 卸载对FastJson和QLExpress的调用
            FastjsonCheck.clearJsonClassLoader();
            FastjsonCheck.clearParseConfigClassLoader();
            QLExpressCheck.clearQLClassLoader();
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


    private static void redefineJavaBaseModule(Instrumentation instrumentation) {
        if (doesSupportModules()) {
            try {
                Instrumentation.class.getMethod("redefineModule", Class.forName("java.lang.Module"), Set.class, Map.class, Map.class, Set.class, Map.class).invoke(instrumentation, getModule(Object.class), Collections.emptySet(), Collections.emptyMap(), Collections.singletonMap("java.lang", Collections.singleton(getModule(EngineManager.class))), Collections.emptySet(), Collections.emptyMap());
            } catch (Exception e) {
                DongTaiLog.error(ErrorCode.REDEFINE_MODULE_FAILED,e);
            }
        }
    }

    public static boolean doesSupportModules() {
        try {
            Class.forName("java.lang.Module");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Object getModule(Class<?> clazz) {
        try {
            return Class.class.getMethod("getModule", new Class[0]).invoke(clazz, new Object[0]);
        } catch (Exception e) {
            throw new IllegalStateException("There was a problem while getting the module of the class", e);
        }
    }
    public static void inject(Instrumentation inst) {
        if (doesSupportModules()) {
            redefineJavaBaseModule(inst);
        }
    }

}
