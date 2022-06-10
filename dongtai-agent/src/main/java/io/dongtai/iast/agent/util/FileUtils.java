package io.dongtai.iast.agent.util;

import io.dongtai.iast.agent.AgentLauncher;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.log.DongTaiLog;

import java.io.*;
import java.net.URI;

public class FileUtils {
    public static boolean getResourceToFile(String resourceName, String fileName) throws IOException {
        File targetFile = new File(fileName);

        if (!targetFile.exists()) {
            if (!targetFile.getParentFile().exists()) {
                if (!targetFile.getParentFile().mkdirs()) {
                    DongTaiLog.error("用户权限不足，文件创建失败");
                }
            }
            if (!targetFile.createNewFile()) {
                DongTaiLog.error("用户权限不足，文件创建失败");
            }
        }
        if (AgentLauncher.LAUNCH_MODE_AGENT.equals("agent")) {
            InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(resourceName);
            if (is == null) return false;
            FileOutputStream fos = new FileOutputStream(targetFile);
            int length = 0;
            byte[] data = new byte[1024];
            while ((length = is.read(data)) != -1) {
                fos.write(data, 0, length);
            }
            data = null;
            is.close();
            fos.close();
            return true;
        }
        return false;
    }

    public static void confReplace(String path) {
        String temp = "";

        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            // 保存该行前面的内容
            while ((temp = br.readLine()) != null) {
                if (temp.contains("${HOSTNAME_AGENT_ID}")){
                    temp = temp.replace("${HOSTNAME_AGENT_ID}", AgentRegisterReport.getInternalHostName()+"-"+AgentRegisterReport.getAgentFlag().toString());
                }else if (temp.contains("${HOSTNAME}")){
                    temp = temp.replace("${HOSTNAME}",AgentRegisterReport.getInternalHostName());
                }else if (temp.contains("${AGENT_ID}")){
                    temp = temp.replace("${AGENT_ID}",AgentRegisterReport.getAgentFlag().toString());
                }else if (temp.contains("${OPENAPI}")){
                    String logAddress = IastProperties.getInstance().getLogAddress();
                    if (null == logAddress){
                        String s = IastProperties.getInstance().getBaseUrl();
                        try {
                            String openApiDomain = new URI(s).getHost();
                            temp = temp.replace("${OPENAPI}", openApiDomain);
                        } catch (Exception e) {
                            s = s.substring(s.indexOf("://") + 3, s.indexOf("/openapi"));
                            if (s.contains(":")) {
                                s = s.substring(0, s.indexOf(":"));
                            }
                            temp = temp.replace("${OPENAPI}", s);
                        }
                    }else {
                        temp = temp.replace("${OPENAPI}", logAddress);
                    }
                }else if (temp.contains("${LOG_PORT}")){
                    String logPort = IastProperties.getInstance().getLogPort();
                    if (null == logPort){
                        String s = IastProperties.getInstance().getBaseUrl();
                        try {
                            int openApiPort = new URI(s).getPort();
                            temp = temp.replace("${LOG_PORT}", openApiPort > 0 ? Integer.toString(openApiPort) :
                                    ("https".equalsIgnoreCase(new URI(s).getScheme()) ? "443" : "80"));
                        } catch (Exception e) {
                            s = s.substring(s.indexOf("://") + 3, s.indexOf("/openapi"));
                            if (s.contains(":")) {
                                s = s.substring(s.indexOf(":") + 1);
                                temp = temp.replace("${LOG_PORT}", s);
                            } else {
                                temp = temp.replace("${LOG_PORT}", "80");
                            }
                        }
                    }else {
                        temp = temp.replace("${LOG_PORT}", logPort);
                    }
                }else if (temp.contains("${LOG_PATH}")){
                    temp = temp.replace("${LOG_PATH}", System.getProperty("java.io.tmpdir.dongtai")+File.separator+"dongtaiJavaAgentLogs"+File.separator+"dongtai_javaagent.log");
                }
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
