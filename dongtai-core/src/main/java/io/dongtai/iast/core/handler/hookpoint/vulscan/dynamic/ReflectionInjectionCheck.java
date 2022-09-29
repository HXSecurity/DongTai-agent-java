package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

import java.util.*;

public class ReflectionInjectionCheck implements SinkSafeChecker {
    private final static String FOR_NAME = "java.lang.Class.forName(java.lang.String)";
    private final static String FOR_NAME_CLASS_LOADER = "java.lang.Class.forName(java.lang.String,boolean,java.lang.ClassLoader)";

    private final static Map<String, HashSet<String>> STACK_BLACKLIST = new HashMap<String, HashSet<String>>() {{
        put(FOR_NAME, new HashSet<String>(Collections.singleton("java.net.URL.getURLStreamHandler")));
        put(FOR_NAME_CLASS_LOADER, new HashSet<String>(Collections.singleton("org.jruby.javasupport.JavaSupport.loadJavaClass")));
    }};

    @Override
    public boolean match(IastSinkModel sink) {
        return STACK_BLACKLIST.containsKey(sink.getSignature());
    }

    @Override
    public boolean isSafe(MethodEvent event, IastSinkModel sink) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        HashSet<String> blacklist = STACK_BLACKLIST.get(sink.getSignature());
        if (blacklist == null) {
            return false;
        }
        for (StackTraceElement stack : stacks) {
            if (blacklist.contains(getClassAndMethodName(stack))) {
                return true;
            }
        }
        return false;
    }

    private String getClassAndMethodName(StackTraceElement stack) {
        return stack.getClassName() + "." + stack.getMethodName();
    }
}
