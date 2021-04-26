package com.secnium.iast.core.enhance.sca;

import org.junit.Test;

import java.io.File;

public class SCAScanTest {

    @Test
    public void scan() {
        /**
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/struts2-core-2.0.8.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/ognl-2.6.11.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/log4j-api-2.11.2.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/jspxcms-9.5.1/WEB-INF/lib/spring-data-jpa-1.11.18.RELEASE.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jasper.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jsp-api.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/el-api.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jasper-el.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/catalina.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/tomcat-util.jar
         * /Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/tomcat-coyote.jar
         *
         */

        String[] packagePaths = new String[]{
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/struts2-core-2.0.8.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/ognl-2.6.11.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/webapps/test_struts2_war/WEB-INF/lib/log4j-api-2.11.2.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jasper.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jasper-el.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/catalina.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/tomcat-util.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/tomcat-coyote.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/el-api.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/jsp-api.jar",
                "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/servlet-api.jar",
                "～/.m2/repository/org/apache/shiro/shiro-web/1.5.2/shiro-web-1.5.2.jar",
                "/tmp/struts2-core-2.0.8.jar"
        };

        for (String packagePath : packagePaths) {
            ScaScanner.scan(new File(packagePath));
        }
    }

    @Test
    public void scanWithJar() {
        String path = "jar:file:～/workspace/secnium/BugPlatflam/dongtai/test-case/springsec/target/iast-vulns.jar!/BOOT-INF/lib/spring-core-5.2.8.RELEASE.jar!/";
        ScaScanner.scanWithJarPackage(path);
    }
}