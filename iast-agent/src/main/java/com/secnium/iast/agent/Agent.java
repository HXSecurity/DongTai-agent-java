package com.secnium.iast.agent;

import com.secnium.iast.agent.manager.EngineManager;
import org.apache.commons.cli.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Agent {


    /**
     * 解析参数、加入tools.jar、调用attach
     *
     * @param args
     */
    public static void main(String[] args) {
        Options attachOptions = new Options();

        attachOptions.addOption(build("p", "pid", "webserver process id"));
        attachOptions.addOption(build("m", "mode", "optional: install uninstall"));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        CommandLine result = null;
        try {
            result = parser.parse(attachOptions, args);
            if (result.hasOption("p") && result.hasOption("m")) {
                String pid = result.getOptionValue("p");
                String mode = result.getOptionValue("m");
                String attachArgs = null;
                attachArgs = mode;

                String jdkVersion = EngineManager.getJdkVersion();
                if ("1".equals(jdkVersion) && appendToolsPath()) {
                    AttachLauncher.attach(pid, attachArgs);
                    LogUtils.info("engine " + attachArgs + " successfully. pid: " + pid);
                } else {
                    AttachLauncher.attach(pid, attachArgs);
                    LogUtils.info("engine " + attachArgs + " successfully. pid: " + pid);
                }
            } else {
                formatter.printHelp("java -jar agent.jar", attachOptions, true);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建option参数对象
     *
     * @param opt     短参数名
     * @param longOpt 长参数名
     * @param desc    参数描述
     * @return 参数对象
     */
    public static Option build(String opt, String longOpt, String desc) {
        return Option.builder(opt).longOpt(longOpt).hasArg().desc(desc).build();
    }

    /**
     * 自动搜索tools.jar包，进行安装
     *
     * @throws NoSuchMethodException
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static boolean appendToolsPath() {
        try {
            File file = new File(new File(System.getProperty("java.home")).getParent(), "lib/tools.jar");
            if (!file.exists()) {
                throw new RuntimeException("Not running with JDK!");
            }
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(urlClassLoader, file.toURI().toURL());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
