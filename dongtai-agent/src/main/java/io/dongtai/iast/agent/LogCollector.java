package io.dongtai.iast.agent;

import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.IOException;

import static io.dongtai.iast.agent.Agent.*;

public class LogCollector {
    private static String FLUENT_FILE;
    private static String FLUENT_FILE_CONF;
    private static Process fluent;
    private static Thread shutdownHook;

    public static void extractFluent() {
        if (IastProperties.getInstance().getLogDisableCollector() || DongTaiLog.getLogPath().isEmpty()) {
            return;
        }
        try {
            if (!isMacOs() && !isWindows()) {
                String agentId = String.valueOf(AgentRegisterReport.getAgentId());
                FLUENT_FILE_CONF = IastProperties.getInstance().getTmpDir() + "fluent-" + agentId + ".conf";
                FileUtils.getResourceToFile("bin/fluent.conf", FLUENT_FILE_CONF);
                FileUtils.confReplace(FLUENT_FILE_CONF);

                String multiParserFile = IastProperties.getInstance().getTmpDir() + "parsers_multiline.conf";
                FileUtils.getResourceToFile("bin/parsers_multiline.conf", multiParserFile);
                FileUtils.confReplace(multiParserFile);

                FLUENT_FILE = IastProperties.getInstance().getTmpDir() + "fluent";
                File f = new File(FLUENT_FILE);
                if (f.exists()) {
                    DongTaiLog.debug("fluent already exists {}", FLUENT_FILE);
                    return;
                }
                if (isArm()) {
                    FileUtils.getResourceToFile("bin/fluent-arm", FLUENT_FILE);
                } else {
                    FileUtils.getResourceToFile("bin/fluent", FLUENT_FILE);
                }

                if (!(new File(FLUENT_FILE)).setExecutable(true)) {
                    DongTaiLog.warn(ErrorCode.FLUENT_SET_EXECUTABLE_FAILED, FLUENT_FILE);
                }
                doFluent();
            }
        } catch (IOException e) {
            DongTaiLog.error(ErrorCode.FLUENT_EXTRACT_FAILED, e);
        }
    }

    public static void doFluent() {
        String[] execution = {
                "nohup",
                FLUENT_FILE,
                "-c",
                FLUENT_FILE_CONF
        };
        try {
            fluent = Runtime.getRuntime().exec(execution);
            DongTaiLog.info("fluent process started");
            shutdownHook = new Thread(new Runnable() {
                @Override
                public void run() {
                    stopFluent();
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IOException e) {
            DongTaiLog.warn(ErrorCode.FLUENT_PROCESS_START_FAILED, e);
        }
    }

    public static void stopFluent() {
        if (fluent == null) {
            return;
        }
        try {
            fluent.destroy();
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            DongTaiLog.info("fluent process stopped");
        } catch (Throwable ignored) {
        } finally {
            fluent = null;
            shutdownHook = null;
        }
    }
}
