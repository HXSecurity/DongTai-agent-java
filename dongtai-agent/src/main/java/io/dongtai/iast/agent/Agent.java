package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Agent {

    private static final String AGENT_PATH = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static String JATTACH_FILE;

    private static String[] parseAgentArgs(String[] args) throws ParseException {
        Options attachOptions = new Options();

        attachOptions.addOption(build("p", "pid", "webserver process id"));
        attachOptions.addOption(build("m", "mode", "optional: install uninstall"));
        attachOptions.addOption(build("debug", "debug", "optional: debug mode"));
        attachOptions.addOption(build("app_create", "app_create", "optional: DongTai Application Auto Create, default: false"));
        attachOptions.addOption(build("app_name", "app_name", "optional: DongTai Application Name, default: ExampleApplication"));
        attachOptions.addOption(build("app_version", "app_version", "optional: DongTai Application Version, default: v1.0"));
        attachOptions.addOption(build("engine_name", "engine_name", "optional: DongTai engine name, default: agent"));
        attachOptions.addOption(build("cluster_name", "cluster_name", "optional: Application Cluster Name"));
        attachOptions.addOption(build("cluster_version", "cluster_version", "optional: Application Cluster Version"));
        attachOptions.addOption(build("dongtai_server", "dongtai_server", "optional: DongTai server url"));
        attachOptions.addOption(build("dongtai_token", "dongtai_token", "optional: DongTai server token"));
        attachOptions.addOption(build("server_package", "server_package", "optional: DongTai core package download way."));
        attachOptions.addOption(build("log", "log", "optional: DongTai agent is log enable, default true."));
        attachOptions.addOption(build("log_level", "log_level", "optional: DongTai agent log print level."));
        attachOptions.addOption(build("log_path", "log_path", "optional: DongTai agent log print path."));
        attachOptions.addOption(build("log_disable_collector", "log_disable_collector", "optional: DongTai agent disable log collector."));
        attachOptions.addOption(build("disabled_plugins", "disabled_plugins", "optional: DongTai agent disable plugins."));
        attachOptions.addOption(build("disabled_features", "disabled_features", "optional: DongTai agent disable features."));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        CommandLine result = null;
        result = parser.parse(attachOptions, args);
        if (result.hasOption("p") && result.hasOption("m")) {
            String pid = result.getOptionValue("p");
            String mode = result.getOptionValue("m");
            StringBuilder attachArgs = new StringBuilder();
            if (isWindows() && AGENT_PATH.startsWith("/")) {
                attachArgs.append(AGENT_PATH.substring(1)).append("=");
            } else {
                attachArgs.append(AGENT_PATH).append("=");
            }

            attachArgs.append("mode=").append(mode);
            for (Map.Entry<String, String> entry : IastProperties.ATTACH_ARG_MAP.entrySet()) {
                if (result.hasOption(entry.getKey())) {
                    attachArgs.append("&").append(entry.getValue()).append("=")
                            .append(result.getOptionValue(entry.getKey()));
                }
            }
            return new String[]{pid, attachArgs.toString()};
        } else {
            formatter.printHelp("java -jar agent.jar", attachOptions, true);
            return null;
        }
    }

    private static void doAttach(String pid, String agentArgs) {
        String[] execution = {
                JATTACH_FILE,
                pid,
                "load",
                "instrument",
                "false",
                agentArgs
        };
        try {
            Process process = Runtime.getRuntime().exec(execution);
            process.waitFor();
            if (process.exitValue() == 0) {
                DongTaiLog.info("attach to process " + pid + " success, command: " + Arrays.toString(execution));
            } else {
                DongTaiLog.error(ErrorCode.JATTACH_EXECUTE_FAILED, Arrays.toString(execution));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isArm() {
        return System.getProperty("os.arch").toLowerCase().contains("arm") || System.getProperty("os.arch").toLowerCase().contains("aarch");
    }

    public static boolean isWindows() {
        return OS_NAME.contains("windows");
    }

    public static boolean isMacOs() {
        return OS_NAME.contains("mac") && OS_NAME.indexOf("os") > 0;
    }

    private static void extractJattach() throws IOException {
        String tmpDir = IastProperties.initTmpDir();
        if (isWindows()) {
            JATTACH_FILE = tmpDir + "jattach.exe";
            FileUtils.getResourceToFile("bin/jattach.exe", JATTACH_FILE);
        } else if (isMacOs()) {
            JATTACH_FILE = tmpDir + "jattach-mac";
            FileUtils.getResourceToFile("bin/jattach-mac", JATTACH_FILE);
        } else if (isArm()) {
            JATTACH_FILE = tmpDir + "jattach-arm";
            FileUtils.getResourceToFile("bin/jattach-arm", JATTACH_FILE);
        } else {
            JATTACH_FILE = tmpDir + "jattach-linux";
            FileUtils.getResourceToFile("bin/jattach-linux", JATTACH_FILE);
        }
        if ((new File(JATTACH_FILE)).setExecutable(true)) {
            DongTaiLog.info("jattach extract success. wait for attach");
        } else {
            DongTaiLog.error(ErrorCode.JATTACH_EXTRACT_FAILED, JATTACH_FILE);
        }
    }

    /**
     * 解析参数、加入tools.jar、调用attach
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        String[] agentArgs = new String[0];
        try {
            agentArgs = parseAgentArgs(args);
            if (agentArgs != null) {
                // todo: 自动搜索需要attach的进程
                extractJattach();
                doAttach(agentArgs[0], agentArgs[1]);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

}
