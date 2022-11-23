package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import io.dongtai.log.DongTaiLog;

import java.util.*;

public class TaintCommandRunner {
    private String signature;

    private TaintRangesBuilder builder;

    private TaintCommand command;

    private List<RunnerParam> params = new ArrayList<RunnerParam>();

    private int paramsCount = 0;

    static class RunnerParam {
        private int position;
        private boolean isLiteral = false;

        public RunnerParam(String param) {
            if (param.startsWith("P")) {
                this.position = Integer.parseInt(param.substring(1)) - 1;
            } else {
                this.position = Integer.parseInt(param);
                this.isLiteral = true;
            }
        }

        public int getParam(Object[] params) {
            if (this.isLiteral) {
                return this.position;
            }
            if (params == null) {
                return 0;
            }

            return (Integer) params[this.position];
        }
    }

    public static TaintCommandRunner create(String signature, TaintCommand command) {
        return create(signature, command, null);
    }

    public static TaintCommandRunner create(String signature, TaintCommand command, List<String> params) {
        try {
            TaintCommandRunner r = new TaintCommandRunner();
            r.signature = signature;
            r.builder = new TaintRangesBuilder();
            r.command = command;
            if (params != null) {
                r.paramsCount = params.size();
                for (String param : params) {
                    r.params.add(new RunnerParam(param));
                }
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public TaintRangesBuilder getTaintRangesBuilder() {
        return this.builder;
    }

    public TaintRanges run(Object source, Object target, Object[] params, TaintRanges oldTaintRanges, TaintRanges srcTaintRanges) {
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;
        TaintRanges tr = new TaintRanges();

        try {
            if (this.paramsCount > 0) {
                p1 = this.params.get(0).getParam(params);
            }
            if (this.paramsCount > 1) {
                p2 = this.params.get(1).getParam(params);
            }
            if (this.paramsCount > 2) {
                p3 = this.params.get(2).getParam(params);
            }
        } catch (Exception e) {
            DongTaiLog.error(this.signature + " taint command parameters fetch failed: " + e.getMessage());
            return tr;
        }

        switch (this.command) {
            case KEEP:
                this.builder.keep(tr, target, this.paramsCount, srcTaintRanges);
                break;
            case APPEND:
                this.builder.append(tr, target, oldTaintRanges, source, srcTaintRanges, p1, p2, this.paramsCount);
                break;
            case SUBSET:
                this.builder.subset(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                break;
            case INSERT:
                this.builder.insert(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                break;
            case REPLACE:
                this.builder.replace(tr, target, oldTaintRanges, source, srcTaintRanges, p1, p2, this.paramsCount);
                break;
            case REMOVE:
                this.builder.remove(tr, source, srcTaintRanges, p1, p2, this.paramsCount);
                break;
            case CONCAT:
                this.builder.concat(tr, target, oldTaintRanges, source, srcTaintRanges, params);
                break;
            case TRIM:
            case TRIM_LEFT:
            case TRIM_RIGHT:
                this.builder.trim(this.command, tr, source, srcTaintRanges, this.paramsCount);
                break;
            default:
                break;
        }

        return tr;
    }

    public static TaintCommandRunner getCommandRunner(String signature) {
        return RUNNER_MAP.get(signature);
    }

    private static final Map<String, TaintCommandRunner> RUNNER_MAP = new HashMap<String, TaintCommandRunner>() {{
        // KEEP String
        String METHOD = "java.lang.String.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(java.lang.StringBuilder)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(byte[],int,int,int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(byte[],int,int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(char[])";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(byte[],java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.<init>(byte[],byte)";    // Java-17
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.String.toLowerCase(java.util.Locale)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.String.toUpperCase(java.util.Locale)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.String.getBytes()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.String.getBytes(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.String.getBytes(java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.String.toCharArray()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R

        // KEEP StringBuilder
        METHOD = "java.lang.StringBuilder.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.StringBuilder.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.StringBuilder.<init>(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O

        // KEEP StringBuffer
        METHOD = "java.lang.StringBuffer.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.lang.StringBuffer.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O
        METHOD = "java.lang.StringBuffer.<init>(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>O

        // KEEP ByteArrayOutputStream
        METHOD = "java.io.ByteArrayOutputStream.toByteArray()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.io.ByteArrayOutputStream.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.io.ByteArrayOutputStream.toString(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.io.ByteArrayOutputStream.toString(int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R
        METHOD = "java.io.ByteArrayOutputStream.toString(java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R

        // KEEP StringConcatHelper
        METHOD = "java.lang.StringConcatHelper.newString(byte[],int,byte)";   // Java 9-11
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>R
        METHOD = "java.lang.StringConcatHelper.newString(byte[],long)";   // Java 12+, up to 14
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>R

        // KEEP StringWriter
        METHOD = "java.io.StringWriter.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // O=>R

        // KEEP
        METHOD = "okhttp3.internal.HostnamesKt.toCanonicalHost(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP)); // P1=>R

        // APPEND String
        METHOD = "java.lang.String.<init>(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>O
        METHOD = "java.lang.String.<init>(char[],int,int,boolean)";    // in IBM JDK8 split()
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>O

        // APPEND StringLatin1/StringUTF16
        METHOD = "java.lang.StringLatin1.newString(byte[],int,int)";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>R
        METHOD = "java.lang.StringUTF16.newString(byte[],int,int)";     // Java-11
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>R

        // APPEND StringBuilder
        METHOD = "java.lang.StringBuilder.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuilder.append(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuilder.append(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuilder.append(java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3"))); // P1=>O
        METHOD = "java.lang.StringBuilder.append(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>O

        // APPEND AbstractStringBuilder
        METHOD = "java.lang.AbstractStringBuilder.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O

        // APPEND StringBuffer
        METHOD = "java.lang.StringBuffer.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuffer.append(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuffer.append(char[])";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuffer.append(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.APPEND)); // P1=>O
        METHOD = "java.lang.StringBuffer.append(java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3"))); // P1=>O
        METHOD = "java.lang.StringBuffer.append(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0"))); // P1=>O

        // APPEND ByteArrayOutputStream
        METHOD = "java.io.ByteArrayOutputStream.write(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3"))); // P1=>O

        // APPEND apache ByteArrayOutputStream
        METHOD = " org.apache.commons.io.output.ByteArrayOutputStream.write(byte[],int,int)".substring(1);
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3"))); // P1=>O

        // APPEND StringWriter
        METHOD = "java.io.StringWriter.write(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3"))); // P1=>O
        METHOD = "java.io.StringWriter.write(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.io.StringWriter.write(java.lang.String,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));

        // SUBSET String
        METHOD = "java.lang.String.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1"))); // O=>R
        METHOD = "java.lang.String.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2"))); // O=>R
        METHOD = "java.lang.String.getBytes(int,int,byte[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));  // O=>P3
        METHOD = "java.lang.String.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));  // O=>P3
        METHOD = "java.lang.String.<init>(byte[],int,int,java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>O

        // SUBSET StringLatin1/StringUTF16 LinesSpliterator
        METHOD = "java.lang.StringLatin1$LinesSpliterator.<init>(byte[],int,int)";      // Java-11
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>O
        METHOD = "java.lang.StringUTF16$LinesSpliterator.<init>(byte[],int,int)";      // Java-11
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>O

        // SUBSET StringBuilder
        METHOD = "java.lang.StringBuilder.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1"))); // O=>R
        METHOD = "java.lang.StringBuilder.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2"))); // O=>R
        METHOD = "java.lang.StringBuilder.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1"))); // O=>O
        METHOD = "java.lang.StringBuilder.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4"))); // O=>P3

        // SUBSET AbstractStringBuilder
        METHOD = "java.lang.AbstractStringBuilder.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1"))); // O=>R
        METHOD = "java.lang.AbstractStringBuilder.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2"))); // O=>R
        METHOD = "java.lang.AbstractStringBuilder.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1"))); // O=>O
        METHOD = "java.lang.AbstractStringBuilder.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4"))); // O=>P3

        // SUBSET StringBuffer
        METHOD = "java.lang.StringBuffer.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1"))); // O=>R
        METHOD = "java.lang.StringBuffer.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2"))); // O=>R
        METHOD = "java.lang.StringBuffer.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1"))); // O=>O
        METHOD = "java.lang.StringBuffer.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4"))); // O=>P3

        // SUBSET ByteBuffer
        METHOD = "java.nio.ByteBuffer.wrap(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R

        // SUBSET Arrays
        METHOD = "java.util.Arrays.copyOf(byte[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P2"))); // P1=>R
        METHOD = "java.util.Arrays.copyOfRange(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R
        METHOD = "java.util.Arrays.copyOf(char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P2"))); // P1=>R
        METHOD = "java.util.Arrays.copyOfRange(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R

        // SUBSET
        METHOD = "okhttp3.HttpUrl$Companion.percentDecode$okhttp(java.lang.String,int,int,boolean)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R
        METHOD = "okhttp3.HttpUrl$Builder.canonicalizeHost(java.lang.String,int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R
        METHOD = "com.squareup.okhttp.HttpUrl$Builder.canonicalizeHost(java.lang.String,int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3"))); // P1=>R

        // INSERT CharArrayReader/PipedReader/PipedInputStream
        METHOD = "java.io.CharArrayReader.<init>(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3"))); // P1=>O
        METHOD = "java.io.CharArrayReader.read(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3"))); // O=>P1
        METHOD = "java.io.PipedReader.read(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));
        METHOD = "java.io.PipedInputStream.read(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));

        // INSERT StringBuilder
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuilder.insert(int,char[])";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuilder.insert(int,char)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4"))); // P2=>O
        METHOD = "java.lang.StringBuilder.insert(int,char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4"))); // P2=>O

        // INSERT StringBuffer
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuffer.insert(int,char[])";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuffer.insert(int,char)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1"))); // P2=>O
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4"))); // P2=>O
        METHOD = "java.lang.StringBuffer.insert(int,char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4"))); // P2=>O

        // REPLACE
        METHOD = "java.lang.StringBuilder.replace(int,int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.REPLACE, Arrays.asList("P1", "P2"))); // P3=>O
        METHOD = "java.lang.StringBuffer.replace(int,int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.REPLACE, Arrays.asList("P1", "P2"))); // P3=>O

        // REMOVE StringBuilder
        METHOD = "java.lang.StringBuilder.delete(int,int)";
        put(METHOD, create(METHOD, TaintCommand.REMOVE, Arrays.asList("P1", "P2"))); // O=>O
        METHOD = "java.lang.StringBuilder.deleteCharAt(int)";
        put(METHOD, create(METHOD, TaintCommand.REMOVE, Collections.singletonList("P1"))); // O=>O

        // REMOVE StringBuffer
        METHOD = "java.lang.StringBuffer.delete(int,int)";
        put(METHOD, create(METHOD, TaintCommand.REMOVE, Arrays.asList("P1", "P2"))); // O=>O
        METHOD = "java.lang.StringBuffer.deleteCharAt(int)";
        put(METHOD, create(METHOD, TaintCommand.REMOVE, Collections.singletonList("P1"))); // O=>O

        // REMOVE ByteArrayOutputStream/apache ByteArrayOutputStream
        METHOD = "java.io.ByteArrayOutputStream.reset()";
        put(METHOD, create(METHOD, TaintCommand.REMOVE)); // O=>O
        METHOD = " org.apache.commons.io.output.ByteArrayOutputStream.reset()".substring(1);
        put(METHOD, create(METHOD, TaintCommand.REMOVE)); // O=>O

        // CONCAT String
        METHOD = "java.lang.String.concat(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.CONCAT)); // O|P1=>R

        // TRIM String
        METHOD = "java.lang.String.strip()";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.TRIM));
        METHOD = "java.lang.String.stripLeading()";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.TRIM_LEFT));
        METHOD = "java.lang.String.stripTrailing()";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.TRIM_RIGHT));
        METHOD = "java.lang.String.trim()";
        put(METHOD, create(METHOD, TaintCommand.TRIM));
    }};
}
