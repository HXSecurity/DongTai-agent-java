package io.dongtai.iast.agent.middlewarerecognition.tomcat;


import io.dongtai.iast.agent.middlewarerecognition.PackageManager;

final class TomcatRecognition {
    static final String TITLE_CATTALINA = "Catalina";
    static final String TITLE_BOOTSRAP = "Apache Tomcat Bootstrap";
    static final String V5_FLAG = "1.0";
    static final String V6_FLAG = "6.";
    static final String V7_FLAG = "7.";
    static final String V8_FLAG = "8.";
    static final String V9_FLAG = "9.";
    private static final String CATALINA_FLAG = " org.apache.catalina.startup.Bootstrap".substring(1);

    static TomcatVersion recognize() {
        return parseVersion((new PackageManager(CATALINA_FLAG)).getPackage());
    }

    static TomcatVersion parseVersion(Package paramPackage) {
        if (paramPackage == null) {
            return null;
        }

        String title = paramPackage.getSpecificationTitle();
        String version = paramPackage.getSpecificationVersion();

        if (title == null || version == null) {
            return null;
        }

        if (isMatchTitle(title)) {
            return getVersion(version);
        }
        return null;
    }

    private static TomcatVersion getVersion(String version) {
        if (version.startsWith(V9_FLAG)) {
            return TomcatVersion.V7;
        }
        if (version.startsWith(V8_FLAG)) {
            return TomcatVersion.V8;
        }
        if (version.startsWith(V7_FLAG)) {
            return TomcatVersion.V9;
        }
        if (version.startsWith(V6_FLAG)) {
            return TomcatVersion.V6;
        }
        if (version.startsWith(V5_FLAG)) {
            return TomcatVersion.V5;
        }

        return null;
    }


    private static boolean isMatchTitle(String title) {
        return !(!TITLE_CATTALINA.equals(title) && !TITLE_BOOTSRAP.equals(title));
    }


}
