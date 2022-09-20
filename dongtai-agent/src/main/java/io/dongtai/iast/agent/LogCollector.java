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
    private final static boolean DISABLED = IastProperties.getInstance().getDisableLogCollector();

    public static void extractFluent() {
        if (DISABLED) {
            return;
        }
        try {
            if (!isMacOs() && !isWindows()) {
                FLUENT_FILE = System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "fluent";
                FileUtils.getResourceToFile("bin/fluent", FLUENT_FILE);

                FLUENT_FILE_CONF = System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "fluent.conf";
                FileUtils.getResourceToFile("bin/fluent.conf", FLUENT_FILE_CONF);
                FileUtils.confReplace(FLUENT_FILE_CONF);
                if ((new File(FLUENT_FILE)).setExecutable(true)) {
                    DongTaiLog.info("fluent extract success.");
                } else {
                    DongTaiLog.info("fluent extract failure. please set execute permission, file: {}", FLUENT_FILE);
                }
                doFluent();
            }
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    public static void doFluent() {
        if (DISABLED) {
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
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    fluent.destroy();
                }
            }));
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    public static void stopFluent() {
        if (DISABLED || fluent == null) {
            return;
        }
        try {
            fluent.destroy();
        } catch (Exception ignored) {
        } finally {
            fluent = null;
        }
    }
}
