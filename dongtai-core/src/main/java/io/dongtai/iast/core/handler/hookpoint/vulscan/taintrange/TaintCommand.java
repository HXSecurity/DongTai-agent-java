package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRangesBuilder.Command;

import java.util.*;

public class TaintCommand {
    public static TaintCommandRunner getCommand(String signature) {
        return runnerMap.get(signature);
    }

    private static final Map<String, TaintCommandRunner> runnerMap = new HashMap<String, TaintCommandRunner>() {{
        // KEEP String
        String STR_INIT_1 = "java.lang.String.<init>(java.lang.String)";
        put(STR_INIT_1, TaintCommandRunner.getInstance(STR_INIT_1, Command.KEEP));
        String STR_INIT_2 = "java.lang.String.<init>(java.lang.StringBuilder)";
        put(STR_INIT_2, TaintCommandRunner.getInstance(STR_INIT_2, Command.KEEP));
        String STR_INIT_3 = "java.lang.String.<init>(java.lang.StringBuffer)";
        put(STR_INIT_3, TaintCommandRunner.getInstance(STR_INIT_3, Command.KEEP));
        String STR_INIT_4 = "java.lang.String.<init>(byte[],int,int)";
        put(STR_INIT_4, TaintCommandRunner.getInstance(STR_INIT_4, Command.KEEP));
        String STR_INIT_5 = "java.lang.String.<init>(byte[],int,int,int)";
        put(STR_INIT_5, TaintCommandRunner.getInstance(STR_INIT_5, Command.KEEP));
        String STR_INIT_6 = "java.lang.String.<init>(byte[],int,int,java.lang.String)";
        put(STR_INIT_6, TaintCommandRunner.getInstance(STR_INIT_6, Command.KEEP));
        String STR_INIT_7 = "java.lang.String.<init>(char[])";
        put(STR_INIT_7, TaintCommandRunner.getInstance(STR_INIT_7, Command.KEEP));
        String STR_INIT_8 = "java.lang.String.<init>(byte[],java.nio.charset.Charset)";
        put(STR_INIT_8, TaintCommandRunner.getInstance(STR_INIT_8, Command.KEEP));
        String STR_INIT_9 = "java.lang.String.<init>(byte[],byte)";     // Java-17
        put(STR_INIT_9, TaintCommandRunner.getInstance(STR_INIT_9, Command.KEEP));
        String STR_LOWER = "java.lang.String.toLowerCase(java.util.Locale)";
        put(STR_LOWER, TaintCommandRunner.getInstance(STR_LOWER, Command.KEEP));
        String STR_UPPER = "java.lang.String.toUpperCase(java.util.Locale)";
        put(STR_UPPER, TaintCommandRunner.getInstance(STR_UPPER, Command.KEEP));
        String STR_GET_BS_1 = "java.lang.String.getBytes()";
        put(STR_GET_BS_1, TaintCommandRunner.getInstance(STR_GET_BS_1, Command.KEEP));
        String STR_GET_BS_2 = "java.lang.String.getBytes(java.lang.String)";
        put(STR_GET_BS_2, TaintCommandRunner.getInstance(STR_GET_BS_2, Command.KEEP));
        String STR_GET_BS_3 = "java.lang.String.getBytes(java.nio.charset.Charset)";
        put(STR_GET_BS_3, TaintCommandRunner.getInstance(STR_GET_BS_3, Command.KEEP));
        String STR_TO_CA = "java.lang.String.toCharArray()";
        put(STR_TO_CA, TaintCommandRunner.getInstance(STR_TO_CA, Command.KEEP));

        // KEEP StringBuilder
        String STR_BUILD_TO_STR = "java.lang.StringBuilder.toString()";
        put(STR_BUILD_TO_STR, TaintCommandRunner.getInstance(STR_BUILD_TO_STR, Command.KEEP));
        String STR_BUILD_INIT_1 = "java.lang.StringBuilder.<init>(java.lang.String)";
        put(STR_BUILD_INIT_1, TaintCommandRunner.getInstance(STR_BUILD_INIT_1, Command.KEEP));
        String STR_BUILD_INIT_2 = "java.lang.StringBuilder.<init>(java.lang.CharSequence)";
        put(STR_BUILD_INIT_2, TaintCommandRunner.getInstance(STR_BUILD_INIT_2, Command.KEEP));

        // KEEP StringBuffer
        String STR_BUFF_TO_STR = "java.lang.StringBuffer.toString()";
        put(STR_BUFF_TO_STR, TaintCommandRunner.getInstance(STR_BUFF_TO_STR, Command.KEEP));
        String STR_BUFF_INIT_1 = "java.lang.StringBuffer.<init>(java.lang.String)";
        put(STR_BUFF_INIT_1, TaintCommandRunner.getInstance(STR_BUFF_INIT_1, Command.KEEP));
        String STR_BUFF_INIT_2 = "java.lang.StringBuffer.<init>(java.lang.CharSequence)";
        put(STR_BUFF_INIT_2, TaintCommandRunner.getInstance(STR_BUFF_INIT_2, Command.KEEP));

        // KEEP ByteArrayOutputStream
        String BAOS_TO_BA = "java.io.ByteArrayOutputStream.toByteArray()";
        put(BAOS_TO_BA, TaintCommandRunner.getInstance(BAOS_TO_BA, Command.KEEP));
        String BAOS_TO_STR_1 = "java.io.ByteArrayOutputStream.toString()";
        put(BAOS_TO_STR_1, TaintCommandRunner.getInstance(BAOS_TO_STR_1, Command.KEEP));
        String BAOS_TO_STR_2 = "java.io.ByteArrayOutputStream.toString(java.lang.String)";
        put(BAOS_TO_STR_2, TaintCommandRunner.getInstance(BAOS_TO_STR_2, Command.KEEP));
        String BAOS_TO_STR_3 = "java.io.ByteArrayOutputStream.toString(int)";
        put(BAOS_TO_STR_3, TaintCommandRunner.getInstance(BAOS_TO_STR_3, Command.KEEP));
        String BAOS_TO_STR_4 = "java.io.ByteArrayOutputStream.toString(java.nio.charset.Charset)";
        put(BAOS_TO_STR_4, TaintCommandRunner.getInstance(BAOS_TO_STR_4, Command.KEEP));

        // KEEP StringConcatHelper
        String STR_CONCAT_HP_NEW_STR_1 = "java.lang.StringConcatHelper.newString(byte[],int,byte)";   // Java 9-11
        put(STR_CONCAT_HP_NEW_STR_1, TaintCommandRunner.getInstance(STR_CONCAT_HP_NEW_STR_1, Command.KEEP));
        String STR_CONCAT_HP_NEW_STR_2 = "java.lang.StringConcatHelper.newString(byte[],long)";   // Java 12+, up to 14
        put(STR_CONCAT_HP_NEW_STR_2, TaintCommandRunner.getInstance(STR_CONCAT_HP_NEW_STR_2, Command.KEEP));

        // KEEP StringWriter
        String STR_WR_TO_STR = "java.io.StringWriter.toString()";
        put(STR_WR_TO_STR, TaintCommandRunner.getInstance(STR_WR_TO_STR, Command.KEEP));

        // APPEND String
        String STR_INIT_APD_1 = "java.lang.String.<init>(char[],int,int)";
        put(STR_INIT_APD_1, TaintCommandRunner.getInstance(STR_INIT_APD_1, Command.APPEND, Arrays.asList("P2", "P3", "0")));
        String STR_INIT_APD = "java.lang.String.<init>(char[],int,int,boolean)";    // in IBM JDK8 split()
        put(STR_INIT_APD, TaintCommandRunner.getInstance(STR_INIT_APD, Command.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND StringBuilder
        String STR_BUILD_APD_1 = "java.lang.StringBuilder.append(java.lang.String)";
        put(STR_BUILD_APD_1, TaintCommandRunner.getInstance(STR_BUILD_APD_1, Command.APPEND));
        String STR_BUILD_APD_2 = "java.lang.StringBuilder.append(java.lang.StringBuffer)";
        put(STR_BUILD_APD_2, TaintCommandRunner.getInstance(STR_BUILD_APD_2, Command.APPEND));
        String STR_BUILD_APD_3 = "java.lang.StringBuilder.append(java.lang.CharSequence)";
        put(STR_BUILD_APD_3, TaintCommandRunner.getInstance(STR_BUILD_APD_3, Command.APPEND));
        String STR_BUILD_APD_4 = "java.lang.StringBuilder.append(java.lang.CharSequence,int,int)";
        put(STR_BUILD_APD_4, TaintCommandRunner.getInstance(STR_BUILD_APD_4, Command.APPEND, Arrays.asList("P2", "P3")));
        String STR_BUILD_APD_5 = "java.lang.StringBuilder.append(char[],int,int)";
        put(STR_BUILD_APD_5, TaintCommandRunner.getInstance(STR_BUILD_APD_5, Command.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND AbstractStringBuilder
        String ABS_STR_BUILD_APD_1 = "java.lang.AbstractStringBuilder.append(java.lang.String)";
        put(ABS_STR_BUILD_APD_1, TaintCommandRunner.getInstance(ABS_STR_BUILD_APD_1, Command.APPEND));

        // APPEND StringBuffer
        String STR_BUFF_APD_1 = "java.lang.StringBuffer.append(java.lang.String)";
        put(STR_BUFF_APD_1, TaintCommandRunner.getInstance(STR_BUFF_APD_1, Command.APPEND));
        String STR_BUFF_APD_2 = "java.lang.StringBuffer.append(java.lang.StringBuffer)";
        put(STR_BUFF_APD_2, TaintCommandRunner.getInstance(STR_BUFF_APD_2, Command.APPEND));
        String STR_BUFF_APD_3 = "java.lang.StringBuffer.append(char[])";
        put(STR_BUFF_APD_3, TaintCommandRunner.getInstance(STR_BUFF_APD_3, Command.APPEND));
        String STR_BUFF_APD_4 = "java.lang.StringBuffer.append(java.lang.CharSequence)";
        put(STR_BUFF_APD_4, TaintCommandRunner.getInstance(STR_BUFF_APD_4, Command.APPEND));
        String STR_BUFF_APD_5 = "java.lang.StringBuffer.append(java.lang.CharSequence,int,int)";
        put(STR_BUFF_APD_5, TaintCommandRunner.getInstance(STR_BUFF_APD_5, Command.APPEND, Arrays.asList("P2", "P3")));
        String STR_BUFF_APD_6 = "java.lang.StringBuffer.append(char[],int,int)";
        put(STR_BUFF_APD_6, TaintCommandRunner.getInstance(STR_BUFF_APD_6, Command.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND ByteArrayOutputStream
        String BAOS_WRITE = "java.io.ByteArrayOutputStream.toString(java.nio.charset.Charset)";
        put(BAOS_WRITE, TaintCommandRunner.getInstance(BAOS_WRITE, Command.APPEND, Arrays.asList("P2", "P3")));

        // APPEND StringLatin1/StringUTF16
        String STR_NEW_STR_1 = "java.lang.StringLatin1.newString(byte[],int,int)";
        put(STR_NEW_STR_1, TaintCommandRunner.getInstance(STR_NEW_STR_1, Command.APPEND, Arrays.asList("P2", "P3", "0")));
        String STR_NEW_STR_2 = "java.lang.StringUTF16.newString(byte[],int,int)";
        put(STR_NEW_STR_2, TaintCommandRunner.getInstance(STR_NEW_STR_2, Command.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND StringWriter
        String STR_WR_WRITE_1 = "java.io.StringWriter.write(char[],int,int)";
        put(STR_WR_WRITE_1, TaintCommandRunner.getInstance(STR_WR_WRITE_1, Command.APPEND, Arrays.asList("P2", "P3")));
        String STR_WR_WRITE_2 = "java.io.StringWriter.write(java.lang.String)";
        put(STR_WR_WRITE_2, TaintCommandRunner.getInstance(STR_WR_WRITE_2, Command.APPEND));
        String STR_WR_WRITE_3 = "java.io.StringWriter.write(java.lang.String,int,int)";
        put(STR_WR_WRITE_3, TaintCommandRunner.getInstance(STR_WR_WRITE_3, Command.APPEND, Arrays.asList("P2", "P3")));

        // APPEND apache ByteArrayOutputStream
        String APACHE_BAOS_WRITE = "org.apache.commons.io.output.ByteArrayOutputStream.write(byte[],int,int)";
        put(APACHE_BAOS_WRITE, TaintCommandRunner.getInstance(APACHE_BAOS_WRITE, Command.APPEND, Arrays.asList("P2", "P3")));
    }};
}
