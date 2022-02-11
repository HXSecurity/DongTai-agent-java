package com.secnium.iast.core.enhance.sca;

import io.dongtai.iast.core.bytecode.sca.SignatureAlgorithm;
import org.junit.Test;

import java.io.File;

public class SignatureTest {

    @Test
    public void getSignture() {
        String algorithm = "SHA-1";
        String filename = "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/catalina.jar";
        File file = new File(filename);
        String signature;

        if (null != (signature = SignatureAlgorithm.getSignature(file, algorithm))) {
            System.out.println(algorithm + ": " + signature);
        }

        algorithm = "MD5";
        if (null != (signature = SignatureAlgorithm.getSignature(file, algorithm))) {
            System.out.println(algorithm + ": " + signature);
        }
    }

    @Test
    public void testGetSignture() {
        String algorithm = "SHA-1";
        String filename = "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/catalina.jar";
        String signature;

        if (null != (signature = SignatureAlgorithm.getSignature(filename, algorithm))) {
            System.out.println(algorithm + ": " + signature);
        }

        algorithm = "MD5";
        if (null != (signature = SignatureAlgorithm.getSignature(filename, algorithm))) {
            System.out.println(algorithm + ": " + signature);
        }

    }
}
