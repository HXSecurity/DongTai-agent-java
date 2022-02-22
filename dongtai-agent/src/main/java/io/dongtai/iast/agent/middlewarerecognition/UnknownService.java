package io.dongtai.iast.agent.middlewarerecognition;

import java.lang.management.RuntimeMXBean;
import java.util.Map;

public class UnknownService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        Map<String, String> properties = runtimeMXBean.getSystemProperties();
        String sunJavaCommand = properties.get("sun.java.command");
        return sunJavaCommand != null && !sunJavaCommand.contains("org.gradle.launcher.GradleMain") && !sunJavaCommand.contains("org.codehaus.plexus.classworlds.launcher.Launcher");
    }

    @Override
    public String getName() {
        return "Unknown";
    }

    @Override
    public String getVersion() {
        return null;
    }
}
