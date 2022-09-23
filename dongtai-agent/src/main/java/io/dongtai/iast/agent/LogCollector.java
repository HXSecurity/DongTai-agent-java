package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.io.IOException;

import static io.dongtai.iast.agent.Agent.*;

public class LogCollector {
    private static String FLUENT_FILE;
    private static String FLUENT_FILE_CONF;
    private static Process fluent;

    public static void extractFluent() {
        if (IastProperties.getInstance().getLogDisableCollector()) {
            return;
        }
        try {
            if (!isMacOs() && !isWindows()) {
                FLUENT_FILE = IastProperties.getInstance().getTmpDir() + "fluent";
                FileUtils.getResourceToFile("bin/fluent", FLUENT_FILE);

                FLUENT_FILE_CONF = IastProperties.getInstance().getTmpDir() + "fluent.conf";
                FileUtils.getResourceToFile("bin/fluent.conf", FLUENT_FILE_CONF);
                FileUtils.confReplace(FLUENT_FILE_CONF);
                if (!(new File(FLUENT_FILE)).setExecutable(true)) {
                    DongTaiLog.info("fluent setExecutable failure. please set execute permission, file: {}", FLUENT_FILE);
                }
                doFluent();
            }
        } catch (IOException e) {
            DongTaiLog.error("fluent extract failure", e);
        }
    }

    public static void doFluent() {
        if (IastProperties.getInstance().getLogDisableCollector()) {
            return;
        }
        String[] execution = {
                "nohup",
                FLUENT_FILE,
                "-c",
                FLUENT_FILE_CONF
        };
        try {
            fluent = Runtime.getRuntime().exec(execution);
            DongTaiLog.info("fluent process started");
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    stopFluent();
                }
            }));
        } catch (IOException e) {
            DongTaiLog.error("fluent process start failed", e);
        }
    }

    public static void stopFluent() {
        if (fluent == null) {
            return;
        }
        try {
            fluent.destroy();
            DongTaiLog.info("fluent process stopped");
        } catch (Exception ignored) {
        } finally {
            fluent = null;
        }
    }
}
