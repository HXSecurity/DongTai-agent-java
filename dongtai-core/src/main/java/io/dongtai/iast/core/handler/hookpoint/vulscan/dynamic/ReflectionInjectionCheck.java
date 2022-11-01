package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;

import java.util.*;

public class ReflectionInjectionCheck implements SinkSafeChecker {
    private final static String FOR_NAME = "java.lang.Class.forName(java.lang.String)";
    private final static String FOR_NAME_CLASS_LOADER = "java.lang.Class.forName(java.lang.String,boolean,java.lang.ClassLoader)";

    private final static Map<String, HashSet<String>> STACK_BLACKLIST = new HashMap<String, HashSet<String>>() {{
        put(FOR_NAME, new HashSet<String>(Collections.singleton("java.net.URL.getURLStreamHandler")));
        put(FOR_NAME_CLASS_LOADER, new HashSet<String>(Collections.singleton("org.jruby.javasupport.JavaSupport.loadJavaClass")));
    }};

    private String policySignature;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return STACK_BLACKLIST.containsKey(this.policySignature);
    }

    @Override
    public boolean isSafe(MethodEvent event, SinkNode sinkNode) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        HashSet<String> blacklist = STACK_BLACKLIST.get(this.policySignature);
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
