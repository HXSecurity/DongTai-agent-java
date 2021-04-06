package com.secnium.iast.core.enhance.sca;

import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ManifestScanner {

    private static String getPackageInfo(Attributes attributes) {
        String version;
        String title;

        version = attributes.getValue("Implementation-Version");
        title = attributes.getValue("Implementation-Title");
        if (title != null && version != null) {
            return title + " " + version;
        }
        return null;
    }

    /**
     * @param jarFile 待检测Manifest的Jar包
     * @return
     * @throws IOException
     */
    public static String parseManifest(JarFile jarFile) throws IOException {
        String filename = null;
        Manifest manifest = jarFile.getManifest();

        Map<String, Attributes> entries = manifest.getEntries();
        for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
            filename = getPackageInfo(entry.getValue());
            if (filename != null) {
                break;
            }
        }
        if (null == filename) {
            filename = getPackageInfo(manifest.getMainAttributes());
        }

        if (null == filename) {
            String[] stages = jarFile.getName().split("/");
            filename = stages[stages.length - 1];
        }
        return filename;
    }
}
