package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.monitor.ServerCommandEnum;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class EngineMonitor implements IMonitor {
    private final EngineManager engineManager;
    public static Boolean isCoreRegisterStart = false;
    private static final String NAME = "EngineMonitor";
    private static Boolean isUninstallHeart = true;
    private String coreStatus = "1";

    public EngineMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + NAME;
    }


    @Override
    public void check() throws Exception {
        String status = checkForStatus();
        if ("1".equals(status) && !"1".equals(coreStatus)) {
            coreStatus = "1";
            DongTaiLog.info("engine start");
            if (EngineManager.checkCoreIsInstalled()) {
                engineManager.start();
            } else {
                startEngine();
            }
        } else if ("2".equals(status) && !"2".equals(coreStatus)) {
            coreStatus = "2";
            DongTaiLog.info("engine stop");
            engineManager.stop();
        }
        HttpClientUtils.sendPost(Constant.API_AGENT_STATUS, HeartBeatReport.generateAgentStatusMsg());
/*        ServerCommandEnum serviceCmdEnum = ServerCommandEnum.getEnum(status);
        if (serviceCmdEnum == null || serviceCmdEnum == ServerCommandEnum.NO_CMD) {
            return;
        }
        DongTaiLog.info("receive system command. cmd:{}, desc:{}", serviceCmdEnum.getCommand(), serviceCmdEnum.getDesc());
        switch (serviceCmdEnum) {
            case CORE_REGISTER_START:
                isCoreRegisterStart = true;
                startEngine();
                break;
            case CORE_START:
                DongTaiLog.info("engine start");
                if(EngineManager.checkCoreIsInstalled()){
                    engineManager.start();
                }else {
                    startEngine();
                }
                break;
            case CORE_STOP:
                DongTaiLog.info("engine stop");
                engineManager.stop();
                break;
            case CORE_UNINSTALL:
                DongTaiLog.info("engine uninstall");
                setIsUninstallHeart(false);
                engineManager.uninstall();
                break;
            case CORE_PERFORMANCE_FORCE_OPEN:
                DongTaiLog.info("force turn on performance breaker");
                forceSwitchPerformanceBreaker(true);
                break;
            case CORE_PERFORMANCE_FORCE_CLOSE:
                DongTaiLog.info("force turn off performance breaker");
                forceSwitchPerformanceBreaker(false);
                break;
            default:
        }*/
    }

    private void forceSwitchPerformanceBreaker(boolean turnOn) {
        try {
            final Class<?> fallbackManagerClass = EngineManager.getFallbackManagerClass();
            if (fallbackManagerClass == null) {
                return;
            }
            fallbackManagerClass.getMethod("invokeSwitchPerformanceBreaker", boolean.class)
                    .invoke(null, turnOn);
        } catch (Throwable t) {
            DongTaiLog.error("turnOnPerformanceBreak failed, msg:{}, err:{}", t.getMessage(), t.getCause());
        }
    }

    private String checkForStatus() {
        try {
            String respRaw = String.valueOf(HttpClientUtils.sendGet(Constant.API_ENGINE_ACTION, "agentId", String.valueOf(AgentRegisterReport.getAgentFlag())));
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                JSONObject data = (JSONObject) resp.get("data");
                return data.get("exceptRunningStatus").toString();
            }
        } catch (Exception e) {
            return "other";
        }
        return "other";
    }

    public void startEngine() {
        boolean status = true;
        if (couldInstallEngine()) {
            // jdk8以上
            status = status && engineManager.extractPackage();
            status = status && engineManager.install();
            status = status && engineManager.start();
        } else {
            // jdk6-7
            status = status && engineManager.extractPackage();
            status = status && engineManager.install();
            status = status && engineManager.start();
        }
        if (!status) {
            DongTaiLog.info("DongTai IAST started failure");
        }
    }

    private boolean couldInstallEngine() {
        // 低版本jdk暂不支持安装引擎core包
        if (JavaVersionUtils.isJava6() || JavaVersionUtils.isJava7()) {
            DongTaiLog.info("DongTai Engine core couldn't install because of low JDK version:" + JavaVersionUtils.javaVersionStr());
            return false;
        }
        return true;
    }

    public static Boolean getIsUninstallHeart() {
        return isUninstallHeart;
    }

    public static void setIsUninstallHeart(Boolean isUninstallHeart) {
        EngineMonitor.isUninstallHeart = isUninstallHeart;
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                try {
                    this.check();
                } catch (Throwable t) {
                    DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
                }
                ThreadUtils.threadSleep(30);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("EngineMonitor interrupted, msg:{}", t.getMessage());
        }
    }
}
