package io.dongtai.iast.agent.manager;

import io.dongtai.iast.agent.IastClassLoader;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.LogCollector;
import io.dongtai.iast.agent.fallback.FallbackManager;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 引擎管理类，负责engine模块的完整生命周期，包括：下载、安装、启动、停止、重启、卸载
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EngineManager {

    private static final String ENGINE_ENTRYPOINT_CLASS = "io.dongtai.iast.core.AgentEngine";
    private static final String INJECT_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-spy";
    private static final String ENGINE_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-core";
    private static final String API_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-api";
    private final static String TMP_DIR = IastProperties.getInstance().getTmpDir();
    private static IastClassLoader IAST_CLASS_LOADER;
    private static EngineManager INSTANCE;
    private static String PID;
    private final Instrumentation inst;
    private final IastProperties properties;
    private final String launchMode;
    private Class<?> classOfEngine;
    private final FallbackManager fallbackManager;
    private final AgentState agentState;

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @param inst       instrumentation接口实例化对象
     * @param launchMode IAST引擎的启动模式，attach、premain两种
     * @param ppid       IAST引擎运行的进程ID，用于后续进行热更新
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance(Instrumentation inst, String launchMode, String ppid, AgentState agentState) {
        if (INSTANCE == null) {
            INSTANCE = new EngineManager(inst, launchMode, ppid, agentState);
        }
        return INSTANCE;
    }

    public static FallbackManager getFallbackManager() {
        return INSTANCE.fallbackManager;
    }

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance() {
        return INSTANCE;
    }

    public EngineManager(Instrumentation inst, String launchMode, String ppid, AgentState agentState) {
        this.inst = inst;
        this.launchMode = launchMode;
        this.properties = IastProperties.getInstance();
        this.fallbackManager = FallbackManager.newInstance(this.properties.cfg);
        this.agentState = agentState;
    }

    /**
     * 获取IAST检测引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return engine包的本地保存路径
     */
    private static String getEnginePackageCachePath() {
        return TMP_DIR + "dongtai-core.jar";
    }

    /**
     * 获取IAST间谍引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return inject包的本地路径
     */
    private static String getInjectPackageCachePath() {
        return TMP_DIR + "dongtai-spy.jar";
    }

    /**
     * 获取IAST间谍引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return inject包的本地路径
     */
    private static String getApiPackagePath() {
        return TMP_DIR + "dongtai-api.jar";
    }

    private static String getGrpcPackagePath() {
        return TMP_DIR + "dongtai-grpc.jar";
    }

    /**
     * 更新IAST引擎需要的jar包，用于启动时加载和热更新检测引擎 - iast-core.jar - iast-inject.jar
     *
     * @return 更新状态，成功为true，失败为false
     */
    public boolean downloadPackageFromServer() {
        // 自定义jar下载地址
        String spyJarUrl = "".equals(properties.getCustomSpyJarUrl()) ? INJECT_PACKAGE_REMOTE_URI : properties.getCustomSpyJarUrl();
        String coreJarUrl = "".equals(properties.getCustomCoreJarUrl()) ? ENGINE_PACKAGE_REMOTE_URI : properties.getCustomCoreJarUrl();
        String apiJarUrl = "".equals(properties.getCustomApiJarUrl()) ? API_PACKAGE_REMOTE_URI : properties.getCustomApiJarUrl();
        return HttpClientUtils.downloadRemoteJar(spyJarUrl, getInjectPackageCachePath()) &&
                HttpClientUtils.downloadRemoteJar(coreJarUrl, getEnginePackageCachePath()) &&
                HttpClientUtils.downloadRemoteJar(apiJarUrl, getApiPackagePath()) &&
                HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-grpc", getGrpcPackagePath());
    }

    /**
     * 从 dongtai-agent.jar 提取相关的jar包
     *
     * @return 提取结果，成功为true，失败为false
     */
    public boolean extractPackageFromAgent() {
        //
        try {
            return FileUtils.getResourceToFile("bin/dongtai-spy.jar", getInjectPackageCachePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-core.jar", getEnginePackageCachePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-api.jar", getApiPackagePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-grpc.jar", getGrpcPackagePath());
        } catch (IOException e) {
            DongTaiLog.error(ErrorCode.AGENT_EXTRACT_PACKAGES_FAILED, e);
        }
        return false;
    }


    public boolean extractPackage() {
        // 解析jar包到本地
        String spyPackage = getInjectPackageCachePath();
        String enginePackage = getEnginePackageCachePath();
        String apiPackage = getApiPackagePath();
        if (properties.isDebug()) {
            DongTaiLog.info("current mode: debug, try to read package from directory {}", TMP_DIR);
            if ((new File(spyPackage)).exists() && (new File(enginePackage)).exists() && (new File(apiPackage)).exists()) {
                return true;
            }
        }
        if ("true".equalsIgnoreCase(properties.getIsDownloadPackage())) {
            return downloadPackageFromServer();
        } else {
            return extractPackageFromAgent();
        }
    }

    public boolean install() {
        String spyPackage = EngineManager.getInjectPackageCachePath();
        String corePackage = EngineManager.getEnginePackageCachePath();
        try {
            JarFile file = new JarFile(new File(spyPackage));
            inst.appendToBootstrapClassLoaderSearch(file);
            file.close();
            if (IAST_CLASS_LOADER == null) {
                IAST_CLASS_LOADER = new IastClassLoader(corePackage);
            }
            classOfEngine = IAST_CLASS_LOADER.loadClass(ENGINE_ENTRYPOINT_CLASS);
            String agentPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            classOfEngine.getMethod("install", String.class, String.class, Integer.class, Instrumentation.class,
                            String.class)
                    .invoke(null, launchMode, this.properties.getPropertiesFilePath(),
                            AgentRegisterReport.getAgentId(), inst, agentPath);
            return true;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_INSTALL_FAILED, e);
        }
        return false;
    }

    /**
     * 启动检测引擎
     */
    public boolean start() {
        try {
            if (classOfEngine != null) {
                classOfEngine.getMethod("start").invoke(null);
                DongTaiLog.info("DongTai engine start successfully.");
                return true;
            }
            return false;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_START_FAILED, e);
        }
        return false;
    }

    /**
     * 停止检测引擎
     *
     * @return 布尔值，表示stop成功或失败
     */
    public boolean stop() {
        try {
            if (classOfEngine != null) {
                classOfEngine.getMethod("stop").invoke(null);
                DongTaiLog.info("DongTai engine stop successfully.");
                return true;
            }
            return false;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_STOP_FAILED, e);
        }
        return false;
    }

    /**
     * 卸载间谍包、检测引擎包
     *
     * @question: 通过 inst.appendToBootstrapClassLoaderSearch() 方法加入的jar包无法直接卸载；
     */
    public synchronized boolean uninstall() {
        try {
            // TODO: state
            if (null == IAST_CLASS_LOADER) {
                return true;
            }

            if (classOfEngine != null) {
                classOfEngine.getMethod("destroy", String.class, String.class, Instrumentation.class)
                        .invoke(null, launchMode, this.properties.getPropertiesFilePath(), inst);
            }

            // 关闭SandboxClassLoader
            classOfEngine = null;
            IAST_CLASS_LOADER.closeIfPossible();
            IAST_CLASS_LOADER = null;
            LogCollector.stopFluent();
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_UNINSTALL_FAILED, e);
        } finally {
            ThreadUtils.killAllDongTaiCoreThreads();
        }
        return true;
    }


    public static String getPID() {
        if (PID == null) {
            String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
            PID = extractPID(runtimeName);
        }
        return PID;
    }

    /**
     * 通过正则提取runtimeName的PID
     * 从开头开始匹配，遇到非数字字符串停止匹配
     * @param runtimeName 运行名称通常为PID@HostName
     * @return PID
     */
    public static String extractPID(String runtimeName){
        Pattern pattern = Pattern.compile("^\\d+");
        Matcher matcher = pattern.matcher(runtimeName);

        //防止极端情况未获取到PID ，设置默认值为0，防止服务端出现问题
        String extractedNumber = "0";
        if (matcher.find()) {
            extractedNumber  = matcher.group(); // 提取匹配到的数字
        }else {
            DongTaiLog.warn("Get PID parsing exception, PID raw data is {}",runtimeName);
        }
        return extractedNumber;
    }

    public AgentState getAgentState() {
        return this.agentState;
    }
}
