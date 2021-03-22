package com.secnium.iast.core.middlewarerecognition.tomcat;

import com.secnium.iast.core.middlewarerecognition.PackageManager;

final class TomcatRecognition {
    static final String TITLE_CATTALINA = "Catalina";
    static final String TITLE_BOOTSRAP = "Apache Tomcat Bootstrap";
    static final String v5Flag = "1.0";
    static final String v6Flag = "6.";
    static final String v7Flag = "7.";
    static final String v8Flag = "8.";
    static final String v9Flag = "9.";
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
        if (version.startsWith(v9Flag)) {
            return TomcatVersion.V7;
        }
        if (version.startsWith(v8Flag)) {
            return TomcatVersion.V8;
        }
        if (version.startsWith(v7Flag)) {
            return TomcatVersion.V9;
        }
        if (version.startsWith(v6Flag)) {
            return TomcatVersion.V6;
        }
        if (version.startsWith(v5Flag)) {
            return TomcatVersion.V5;
        }

        return null;
    }


    private static boolean isMatchTitle(String title) {
        return !(!TITLE_CATTALINA.equals(title) && !TITLE_BOOTSRAP.equals(title));
    }


}
