package io.dongtai.iast.agent.middlewarerecognition.jboss;


import io.dongtai.iast.agent.middlewarerecognition.IServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JBoss中间件起初以jboss命名，直到JBoss 6，从JBoss7开始，更名为JBoss AS 7
 *
 * @author dongzhiyong@huoxian.cn
 */
public class JBoss implements IServer {
    /**
     * 检测是否为JBoss中间件
     * - 类路径中存在：bin/run.jar
     * - Java应用启动命令中包含：org.jboss.Main
     *
     * @param runtimeMXBean 运行时管理器对象
     * @return true: 匹配；false：不匹配
     */
    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        String classPath = runtimeMXBean.getClassPath();
        String command = runtimeMXBean.getSystemProperties().get("sun.java.command");
        return classPath.contains("run.jar") && command.contains("org.jboss.Main");
    }

    @Override
    public String getName() {
        return "Jboss";
    }

    @Override
    public String getVersion() {
        // <jar name="jboss-system.jar" specVersion="6.1.0.Final"
        // 读取jar-versions.xml文件，使用正则匹配：<jar name="jboss-system.jar" specVersion="(.*?)"，即可获取版本号
        String version = "*";
        File versionFile = new File(".", "jar-versions.xml");
        if (versionFile.exists()) {
            File temp = new File(".", "bin" + File.separatorChar + "run.jar");
            if (temp.exists()) {
                try {
                    byte[] arrayOfByte = FileUtils.readFileToByteArray(versionFile);
                    String str = new String(arrayOfByte);
                    Matcher matcher = VER_PATTERN.matcher(str);
                    if (matcher.find()) {
                        version = matcher.group(1);
                    }
                } catch (IOException iOException) {
                    ;
                }
            }
        }
        return version;
    }

    final static Pattern VER_PATTERN = Pattern.compile("<jar name=\"jboss-system.jar\" specVersion=\"(.*?)\"");
}
