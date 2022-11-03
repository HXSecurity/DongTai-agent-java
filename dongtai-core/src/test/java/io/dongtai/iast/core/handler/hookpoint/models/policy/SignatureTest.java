package io.dongtai.iast.core.handler.hookpoint.models.policy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.*;

public class SignatureTest {
    @Test
    public void testParse() {
        Map<String, List<Object>> tests = new HashMap<String, List<Object>>() {{
            put("a.b()", Arrays.asList("a", "b", new String[]{}));
            put("java.lang.String.<init>(java.lang.StringBuilder)",
                    Arrays.asList("java.lang.String", "<init>", new String[]{"java.lang.StringBuilder"}));
            put("java.lang.String.<init>(char[],int,int)",
                    Arrays.asList("java.lang.String", "<init>", new String[]{"char[]", "int", "int"}));
            put("java.lang.String.<init> (char[], int, int)",
                    Arrays.asList("java.lang.String", "<init>", new String[]{"char[]", "int", "int"}));
            put("java.lang.StringBuilder.toString()",
                    Arrays.asList("java.lang.StringBuilder", "toString", new String[]{}));
            put("java.lang.StringBuilder.toString( )",
                    Arrays.asList("java.lang.StringBuilder", "toString", new String[]{}));
        }};

        for (Map.Entry<String, List<Object>> entry : tests.entrySet()) {
            Signature signature = Signature.parse(entry.getKey());
            Assert.assertEquals("parse " + entry.getKey(), entry.getValue().get(0), signature.getClassName());
            Assert.assertEquals("parse " + entry.getKey(), entry.getValue().get(1), signature.getMethodName());
            Assert.assertArrayEquals("parse " + entry.getKey(), (String[]) entry.getValue().get(2), signature.getParameters());
        }

        IllegalArgumentException exception;

        Map<String, String> exceptionTests = new HashMap<String, String>() {{
            put(null, Signature.ERR_SIGNATURE_EMPTY);
            put("", Signature.ERR_SIGNATURE_EMPTY);
            put(" ", Signature.ERR_SIGNATURE_EMPTY);
            put("String", Signature.ERR_SIGNATURE_INVALID);
            put("String(", Signature.ERR_SIGNATURE_INVALID);
            put("String)", Signature.ERR_SIGNATURE_INVALID);
            put("()", Signature.ERR_SIGNATURE_INVALID);
            put("a()", Signature.ERR_SIGNATURE_INVALID);
            put("a.()", Signature.ERR_SIGNATURE_INVALID);
            put(".a()", Signature.ERR_SIGNATURE_INVALID);
            put("String()", Signature.ERR_SIGNATURE_INVALID);
            put("String.split", Signature.ERR_SIGNATURE_INVALID);
            put("String.split()A", Signature.ERR_SIGNATURE_INVALID);
            put("String.split)(", Signature.ERR_SIGNATURE_INVALID);
            put("String.split())", Signature.ERR_SIGNATURE_INVALID);
            put("String.split(()", Signature.ERR_SIGNATURE_INVALID);
        }};

        for (Map.Entry<String, String> entry : exceptionTests.entrySet()) {
            exception = Assert.assertThrows("parse exception " + entry.getKey(),
                IllegalArgumentException.class, new ThrowingRunnable() {
                    @Override
                    public void run() throws IllegalArgumentException {
                        Signature.parse(entry.getKey());
                    }
                });
            Assert.assertTrue("parse exception " + entry.getKey(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testNormalizeSignature() {
        Map<List<Object>, String> tests = new HashMap<List<Object>, String>() {{
            put(Arrays.asList("java.lang.String", "<init>", new String[]{"java.lang.StringBuilder"}),
                    "java.lang.String.<init>(java.lang.StringBuilder)");
            put(Arrays.asList("java.lang.String", "<init>", new String[]{"char[]", "int", "int"}),
                    "java.lang.String.<init>(char[],int,int)");
            put(Arrays.asList("java.lang.StringBuilder", "toString", new String[]{}),
                    "java.lang.StringBuilder.toString()");
        }};

        for (Map.Entry<List<Object>, String> entry : tests.entrySet()) {
            String signature = Signature.normalizeSignature((String) entry.getKey().get(0),
                    (String) entry.getKey().get(1), (String[]) entry.getKey().get(2));
            Assert.assertEquals("normalizeSignature " + entry.getValue(), entry.getValue(), signature);
        }
    }

    @Test
    public void testEquals() {
        Map<String, String> tests = new HashMap<String, String>() {{
            put("java.lang.String.<init>(char[],int,int)", "java.lang.String.<init> (char[], int, int)");
            put("java.lang.StringBuilder.toString()", "java.lang.StringBuilder.toString( )");
        }};

        for (Map.Entry<String, String> entry : tests.entrySet()) {
            Assert.assertEquals("equals " + entry.getKey(),
                    Signature.parse(entry.getKey()), Signature.parse(entry.getValue()));
        }
    }
}