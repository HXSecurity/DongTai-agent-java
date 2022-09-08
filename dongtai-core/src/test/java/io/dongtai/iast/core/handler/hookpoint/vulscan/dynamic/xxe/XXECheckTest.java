package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XXECheckTest {
    public final static String SAFE_OR_BLIND = "safe empty node or blind";

    public final static String LINUX_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>";
    public final static String WINDOWS_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///c:/windows/win.ini\" >]><foo>&xxe;</foo>";

    public boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }

    public String getPayload() {
        if (isWindows()) {
            return WINDOWS_PAYLOAD;
        }
        return LINUX_PAYLOAD;
    }

    public String getXXERealContent() {
        String filename = "/etc/passwd";
        if (isWindows()) {
            filename = "c:/windows/win.ini";
        }

        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return new String(data, StandardCharsets.UTF_8).replaceAll("\\r\\n?", "\n");
        } catch (IOException e) {
            System.out.println("XXE check test get real content failed: " + e.toString());
            return "";
        }
    }
}
