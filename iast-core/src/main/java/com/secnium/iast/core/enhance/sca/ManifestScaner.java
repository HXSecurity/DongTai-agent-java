package com.secnium.iast.core.enhance.sca;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ManifestScaner {

    private static String getPackgeInfo(Attributes attributes) {
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
     * todo: 考虑提取pom.xml或pom.properties，进行依赖的深度解析
     *
     * @param jarFile
     * @return
     * @throws IOException
     */
    public static String parseJarManifest(JarFile jarFile) throws IOException {
        String filename = null;
        Manifest manifest = jarFile.getManifest();

        Map<String, Attributes> entries = manifest.getEntries();
        for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
            filename = getPackgeInfo(entry.getValue());
            if (filename != null) {
                break;
            }
        }
        if (null == filename) {
            filename = getPackgeInfo(manifest.getMainAttributes());
        }

        if (null == filename) {
            String[] stages = jarFile.getName().split("/");
            filename = stages[stages.length - 1];
        }
        return filename;
    }

    public static String parseJarManifest(File file) throws IOException {
        return parseJarManifest(new JarFile(file));
    }
}
