package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.log.DongTaiLog;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

public class HttpImplTest {
    @Test
    public void testInitJarPackage() {
        File IAST_REQUEST_JAR_PACKAGE;
        try {
            InputStream inputStream = HttpImpl.class.getClassLoader().getResourceAsStream("dongtai-servlet.jar");
            assert inputStream != null;

            IAST_REQUEST_JAR_PACKAGE = new File("/tmp/dongtai-servlet-test.jar");
            FileOutputStream outputStream = new FileOutputStream(IAST_REQUEST_JAR_PACKAGE);
            byte[] data = new byte[1000];
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            DongTaiLog.error(e);
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    @Test
    public void testUrlConnectInitJarPackage() {
        File IAST_REQUEST_JAR_PACKAGE = new File("/tmp/dongtai-servlet-test.jar");
        try {
            URL jarUrl = HttpImpl.class.getClassLoader().getResource("dongtai-servlet.jar");
            URLConnection connection = jarUrl.openConnection();
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            FileOutputStream outputStream = new FileOutputStream(IAST_REQUEST_JAR_PACKAGE);

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
            }
            new JarFile(new File("/Users/shengnanwu/workspace/secnium/BugPlatflam/DongTai/agent/dongtai-agent-java/dongtai-core/src/main/resources/dongtai-servlet.jar"));
            System.out.println("try open sub jar");
            JarFile jarFile = new JarFile(IAST_REQUEST_JAR_PACKAGE);
        } catch (FileNotFoundException e) {
            DongTaiLog.error(e);
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

//    @Test
//    public boolean downloadJarPackageToCacheFromUrl() {
//        String fileUrl, fileName;
//        boolean status = false;
//        try {
//            URL url = new URL(fileUrl);
//            Proxy proxy = UpdateUtils.loadProxy();
//            HttpURLConnection connection = proxy == null ? (HttpURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection(proxy);
//
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("User-Agent", "DongTai-IAST-Agent");
//            connection.setRequestProperty("Authorization", "Token " + properties.getIastServerToken());
//            connection.setUseCaches(false);
//            connection.setDoOutput(true);
//
//            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
//            final File classPath = new File(new File(fileName).getParent());
//
//            if (!classPath.mkdirs() && !classPath.exists()) {
//                System.out.println("[cn.huoxian.dongtai.iast] Check or create local file cache path, path is " + classPath);
//            }
//            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
//            byte[] dataBuffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//                fileOutputStream.write(dataBuffer, 0, bytesRead);
//            }
//            System.out.println("[cn.huoxian.dongtai.iast] The remote file " + fileUrl + " was successfully written to the local cache.");
//            status = true;
//        } catch (Exception ignore) {
//            System.err.println("[cn.huoxian.dongtai.iast] The remote file " + fileUrl + " download failure, please check the iast-token.");
//        }
//        return status;
//    }
}
