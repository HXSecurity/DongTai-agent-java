package com.secnium.iast.agent.manager;

import com.secnium.iast.agent.*;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

/**
 * 引擎管理类，负责engine模块的完整生命周期，包括：下载、安装、启动、停止、重启、卸载
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EngineManager {
    private static final String IAST_NAMESPACE = "DONGTAI";
    private static final String ENGINE_ENTRYPOINT_CLASS = "com.secnium.iast.core.AgentEngine";
    private static final String INJECT_PACKAGE_REMOTE_URI = "/api/v1/engine/download?package_name=iast-inject&jdk.version=";
    private static final String ENGINE_PACKAGE_REMOTE_URI = "/api/v1/engine/download?package_name=iast-core&jdk.version=";
    private static final Map<String, IastClassLoader> IAST_CLASS_LOADER_CACHE = new ConcurrentHashMap<String, IastClassLoader>();
    private static EngineManager INSTANCE;

    private final Instrumentation inst;
    private int runningStatus;
    private final IastProperties properties;
    private final String launchMode;
    private Class<?> classOfEngine;
    private final String ppid;

    /**
     * 获取IAST引擎的启动状态
     *
     * @return 启动状态
     */
    public int getRunningStatus() {
        return runningStatus;
    }

    /**
     * 设置IAST引擎的启动状态
     *
     * @param runningStatus 启动状态
     */
    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
    }

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @param inst       instrumentation接口实例化对象
     * @param launchMode IAST引擎的启动模式，attach、premain两种
     * @param ppid       IAST引擎运行的进程ID，用于后续进行热更新
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance(Instrumentation inst, String launchMode, String ppid) {
        if (INSTANCE == null) {
            INSTANCE = new EngineManager(inst, launchMode, ppid);
        }
        return INSTANCE;
    }

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance() {
        return INSTANCE;
    }

    public EngineManager(Instrumentation inst, String launchMode, String ppid) {
        this.inst = inst;
        this.runningStatus = 0;
        this.launchMode = launchMode;
        this.properties = IastProperties.getInstance();
        this.ppid = ppid;
    }

    /**
     * 获取IAST检测引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return engine包的本地保存路径
     */
    public static String getEnginePackageCachePath() {
        return System.getProperty("java.io.tmpdir") + File.separator + "iast-core.jar";
    }

    /**
     * 获取IAST间谍引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return inject包的本地路径
     */
    public static String getInjectPackageCachePath() {
        return System.getProperty("java.io.tmpdir") + File.separator + "iast-inject.jar";
    }

    /**
     * 从远程URI下载jar包到指定的本地文件
     *
     * @param fileUrl  远程URI
     * @param fileName 本地文件路径
     * @return 下载结果，成功为true，失败为false
     */
    private boolean downloadJarPackageToCacheFromUrl(String fileUrl, String fileName) {
        boolean status = false;
        try {
            URL url = new URL(fileUrl);
            Proxy proxy = UpdateUtils.loadProxy();
            HttpURLConnection connection = proxy == null ? (HttpURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection(proxy);

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SecniumIast Agent");
            connection.setRequestProperty("Authorization", "Token " + properties.getIastServerToken());
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            final File classPath = new File(new File(fileName).getParent());

            if (!classPath.mkdirs() && !classPath.exists()) {
                System.out.println("[cn.huoxian.dongtai.iast] Check or create local file cache path, path is " + classPath);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("[cn.huoxian.dongtai.iast] The remote file " + fileUrl + " was successfully written to the local cache.");
            status = true;
        } catch (Exception ignore) {
            System.err.println("[cn.huoxian.dongtai.iast] The remote file " + fileUrl + " download failure, please check the iast-token.");
        }
        return status;
    }

    /**
     * 更新IAST引擎需要的jar包，用于启动时加载和热更新检测引擎
     * - iast-core.jar
     * - iast-inject.jar
     *
     * @return 更新状态，成功为true，失败为false
     */
    public boolean updateEnginePackage() {
        String jdkVersion = getJdkVersion();
        String baseUrl = properties.getBaseUrl();
        if (downloadJarPackageToCacheFromUrl(baseUrl + INJECT_PACKAGE_REMOTE_URI + jdkVersion, getInjectPackageCachePath()) &&
                downloadJarPackageToCacheFromUrl(baseUrl + ENGINE_PACKAGE_REMOTE_URI + jdkVersion, getEnginePackageCachePath())) {
            UpdateUtils.setUpdateSuccess();
            return true;
        }
        return false;
    }


    public boolean downloadEnginePackage() {
        System.out.println("[cn.huoxian.dongtai.iast] Check if the engine needs to be updated");
        if (UpdateUtils.checkForUpdate()) {
            System.out.println("[cn.huoxian.dongtai.iast] Receive an instruction from the remote server to update the engine, update the engine immediately");
            return updateEnginePackage();
        } else {
            if (engineNotExist(getInjectPackageCachePath()) || engineNotExist(getEnginePackageCachePath())) {
                System.out.println("[cn.huoxian.dongtai.iast] Engine does not exist in local cache, the engine will be downloaded.");
                return updateEnginePackage();
            } else {
                return true;
            }
        }
    }

    /**
     * 向BootstrapClassLoader中注册间谍包、加载检测引擎所在jar包
     */
    public boolean install() {
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(EngineManager.getInjectPackageCachePath())));
            ClassLoader iastClassLoader = IAST_CLASS_LOADER_CACHE.get(IAST_NAMESPACE);
            if (iastClassLoader == null) {
                iastClassLoader = loadOrDefineClassLoader(EngineManager.getEnginePackageCachePath());
            }
            classOfEngine = iastClassLoader.loadClass(ENGINE_ENTRYPOINT_CLASS);
            classOfEngine.getMethod("install", String.class, String.class, Instrumentation.class)
                    .invoke(null, launchMode, this.properties.getPropertiesFilePath(), inst);
            return true;
        } catch (IOException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine installation failed, please contact staff for help.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine installation failed, please contact staff for help.");
            e.printStackTrace();
        } catch (Throwable throwable) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine installation failed, please contact staff for help.");
            throwable.printStackTrace();
        }
        return false;
    }

    /**
     * 启动检测引擎
     */
    public boolean start() {
        // 将Spy注入到BootstrapClassLoader，todo: 异常卸载时，需要特定处理spy模块
        try {
            if (classOfEngine != null) {
                classOfEngine.getMethod("start").invoke(null);
                return true;
            }
            return false;
        } catch (InvocationTargetException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine start failed, please contact staff for help.");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine start failed, please contact staff for help.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine start failed, please contact staff for help.");
            e.printStackTrace();
        } catch (Throwable throwable) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine start failed, please contact staff for help.");
            throwable.printStackTrace();
        }
        return false;
    }

    /**
     * 停止检测引擎
     *
     * @return 布尔值，表示stop成功或失败
     */
    public boolean stop() {
        // 将Spy注入到BootstrapClassLoader，todo: 异常卸载时，需要特定处理spy模块
        try {
            if (classOfEngine != null) {
                classOfEngine.getMethod("stop").invoke(null);
                return true;
            }
            return false;
        } catch (InvocationTargetException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine stop failed, please contact staff for help.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println(sw.toString());
        } catch (NoSuchMethodException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine stop failed, please contact staff for help.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine stop failed, please contact staff for help.");
            e.printStackTrace();
        } catch (Throwable throwable) {
            System.err.println("[cn.huoxian.dongtai.iast] DongTai engine stop failed, please contact staff for help.");
            throwable.printStackTrace();
        }
        return false;
    }

    /**
     * 重启检测引擎
     *
     * @return true-重启成功；false-重启失败
     */
    public boolean restart() {
        return true;
    }

    /**
     * 卸载间谍包、检测引擎包
     *
     * @question: 通过 inst.appendToBootstrapClassLoaderSearch() 方法加入的jar包无法直接卸载；
     */
    public synchronized boolean uninstall() {
        final IastClassLoader classLoader = IAST_CLASS_LOADER_CACHE.get(IAST_NAMESPACE);
        if (null == classLoader) {
            return true;
        }

        try {
            // 卸载字节码
            if (classOfEngine != null) {
                classOfEngine.getMethod("destroy", String.class, String.class, Instrumentation.class)
                        .invoke(null, launchMode, this.properties.getPropertiesFilePath(), inst);
                Agent.appendToolsPath();
                AttachLauncher.detach(ppid);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 关闭SandboxClassLoader
        classOfEngine = null;
        classLoader.closeIfPossible();

        IAST_CLASS_LOADER_CACHE.remove(IAST_NAMESPACE);
        return true;
    }

    /**
     * 使用IAST类加载器加载检测引擎
     *
     * @param coreJar 检测引擎的物理路径
     * @return 类加载器
     * @throws Throwable
     */
    private static synchronized ClassLoader loadOrDefineClassLoader(final String coreJar) throws Throwable {

        final IastClassLoader classLoader;

        // 如果已经被启动则返回之前启动的ClassLoader
        if (IAST_CLASS_LOADER_CACHE.containsKey(EngineManager.IAST_NAMESPACE)
                && null != IAST_CLASS_LOADER_CACHE.get(EngineManager.IAST_NAMESPACE)) {
            classLoader = IAST_CLASS_LOADER_CACHE.get(EngineManager.IAST_NAMESPACE);
        }

        // 如果未启动则重新加载
        else {
            classLoader = new IastClassLoader(EngineManager.IAST_NAMESPACE, coreJar);
            IAST_CLASS_LOADER_CACHE.put(EngineManager.IAST_NAMESPACE, classLoader);
        }

        return classLoader;
    }

    /**
     * 检测引擎是否不存在
     *
     * @param jarPath 引擎的物理路径
     * @return true-引擎不存在；false-引擎存在
     */
    private static boolean engineNotExist(final String jarPath) {
        String isDebug = System.getProperty("debug");
        if ("true".equals(isDebug)) {
            System.out.println("[cn.huoxian.dongtai.iast] current mode: debug, load engine from " + jarPath);
            File tempFile = new File(jarPath);
            return !tempFile.exists();
        } else {
            return true;
        }
    }

    /**
     * 判断jdk版本，根据jdk版本下载不同版本的java agent
     *
     * @return 1 - jdk 1.6~1.8；2 - jdk 1.9及以上
     */
    private static String getJdkVersion() {
        String jdkVersion = System.getProperty("java.version", "1.8");
        System.out.println("current jdk version is : " + jdkVersion);
        String[] jdkVersionItem = jdkVersion.split("\\.");
        boolean isHighJdk = true;
        if (jdkVersionItem.length > 1 && ("6".equals(jdkVersionItem[1]) || "7".equals(jdkVersionItem[1]) || "8".equals(jdkVersionItem[1]))) {
            isHighJdk = false;
        }
        return isHighJdk ? "2" : "1";
    }

}
