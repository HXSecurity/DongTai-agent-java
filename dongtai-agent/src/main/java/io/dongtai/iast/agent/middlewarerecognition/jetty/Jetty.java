package io.dongtai.iast.agent.middlewarerecognition.jetty;


import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.log.DongTaiLog;

import java.io.*;
import java.lang.management.RuntimeMXBean;

/**
 * 已适配：直接下载源码包安装的场景
 * 如果类路径中存在start.jar，则认为是jetty，如何区分jetty的不同版本
 * <p>
 * jetty8 启动
 * - jetty启动完整命令：
 * - jetty启动Java命令：org.eclipse.jetty.start.Main /private/var/folders/bz/38vyth2d7_bfrlkvklncp_880000gn/T/contextconfig/contexts-config.xml
 * <p>
 * jetty9 启动
 *
 * @author dongzhiyong@huoxian.cn
 */
public class Jetty implements IServer {

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        String classPath = runtimeMXBean.getClassPath();
        String javaCommand = runtimeMXBean.getSystemProperties().get("sun.java.command");
        return classPath.contains("start.jar") && javaCommand.contains("org.eclipse.jetty.start.Main");
    }

    @Override
    public String getName() {
        return "Jetty";
    }

    @Override
    public String getVersion() {
        File versionFile = null;
        FileReader fileReader = null;
        LineNumberReader reader = null;
        String version = "x";
        try {
            versionFile = new File(".", "VERSION.txt");
            fileReader = new FileReader(versionFile);
            reader = new LineNumberReader(fileReader);
            String temp = reader.readLine();
            version = temp.split(" ")[0];
            reader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            DongTaiLog.error(e);
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
        return version;
    }
}
