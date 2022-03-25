package io.dongtai.iast.agent;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.cli.*;

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
        attachOptions.addOption(build("app_name", "app_name", "optional: DongTai Application Name, default: ExampleApplication"));
        attachOptions.addOption(build("app_create", "app_create", "optional: DongTai Application Auto Create, default: false"));
        attachOptions.addOption(build("app_version", "app_version", "optional: DongTai Application Version, default: v1.0"));
        attachOptions.addOption(build("cluster_name", "cluster_name", "optional: Application Cluster Name"));
        attachOptions.addOption(build("cluster_version", "cluster_version", "optional: Application Cluster Version"));
        attachOptions.addOption(build("dongtai_server", "dongtai_server", "optional: DongTai server url"));
        attachOptions.addOption(build("dongtai_token", "dongtai_token", "optional: DongTai server token"));

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
            if (result.hasOption("debug")) {
                attachArgs.append("&debug=").append(result.getOptionValue("debug"));
            }
            if (result.hasOption("app_create")) {
                attachArgs.append("&appCreate=").append(result.getOptionValue("app_create"));
            }
            if (result.hasOption("app_name")) {
                attachArgs.append("&appName=").append(result.getOptionValue("app_name"));
            }
            if (result.hasOption("app_version")) {
                attachArgs.append("&appVersion=").append(result.getOptionValue("app_version"));
            }
            if (result.hasOption("cluster_name")) {
                attachArgs.append("&clusterName=").append(result.getOptionValue("cluster_name"));
            }
            if (result.hasOption("cluster_version")) {
                attachArgs.append("&clusterVersion=").append(result.getOptionValue("cluster_version"));
            }
            if (result.hasOption("dongtai_server")) {
                attachArgs.append("&dongtaiServer=").append(result.getOptionValue("dongtai_server"));
            }
            if (result.hasOption("dongtai_token")) {
                attachArgs.append("&dongtaiToken=").append(result.getOptionValue("dongtai_token"));
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
                DongTaiLog.info("attach to process {} success, command: {}", pid, Arrays.toString(execution));
            } else {
                DongTaiLog.error("attach failure, please try again with command: {}", Arrays.toString(execution));
            }
        } catch (IOException e) {
            DongTaiLog.error(e);
        } catch (InterruptedException e) {
            DongTaiLog.error(e);
        }
    }

    private static boolean isLinux() {
        return OS_NAME.indexOf("linux") >= 0;
    }

    private static boolean isWindows() {
        return OS_NAME.indexOf("windows") >= 0;
    }

    private static boolean isMacOs() {
        return OS_NAME.indexOf("mac") >= 0 && OS_NAME.indexOf("os") > 0;
    }

    private static void extractJattach() throws IOException {
        if (isWindows()) {
            JATTACH_FILE = System.getProperty("java.io.tmpdir.dongtai") + File.separator + "iast" + File.separator + "jattach.exe";
            FileUtils.getResourceToFile("bin/jattach.exe", JATTACH_FILE);
        } else if (isMacOs()) {
            JATTACH_FILE = System.getProperty("java.io.tmpdir.dongtai") + File.separator + "iast" + File.separator + "jattach-mac";
            FileUtils.getResourceToFile("bin/jattach-mac", JATTACH_FILE);
        } else {
            JATTACH_FILE = System.getProperty("java.io.tmpdir.dongtai") + File.separator + "iast" + File.separator + "jattach-linux";
            FileUtils.getResourceToFile("bin/jattach-linux", JATTACH_FILE);
        }
        if ((new File(JATTACH_FILE)).setExecutable(true)) {
            DongTaiLog.info("jattach extract success. wait for attach");
        } else {
            DongTaiLog.info("jattach extract failure. please set execute permission, file: {}", JATTACH_FILE);
        }
    }

    /**
     * 解析参数、加入tools.jar、调用attach
     *
     * @param args
     */
    public static void main(String[] args) {
        String[] agentArgs = new String[0];
        try {
            agentArgs = parseAgentArgs(args);
            if (agentArgs != null) {
                // todo: 自动搜索需要attach的进程
                extractJattach();
                doAttach(agentArgs[0], agentArgs[1]);
            }
        } catch (ParseException e) {
            DongTaiLog.error(e);
        } catch (IOException e) {
            DongTaiLog.error(e);
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
