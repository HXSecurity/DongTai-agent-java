package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRangesBuilder.Command;

import java.util.HashMap;
import java.util.Map;

public class TaintCommand {
    private static Map<String, TaintCommandRunner> runnerMap = new HashMap<String, TaintCommandRunner>(){{
        String STR_BUILD_INIT_1 = "java.lang.StringBuilder.<init>(java.lang.String)";
        put(STR_BUILD_INIT_1, TaintCommandRunner.getInstance(STR_BUILD_INIT_1, Command.KEEP));
        String STR_BUILD_TO_STR = "java.lang.StringBuilder.toString()";
        put(STR_BUILD_TO_STR, TaintCommandRunner.getInstance(STR_BUILD_TO_STR, Command.KEEP));

        String STR_BUILD_APPEND_1 = "java.lang.StringBuilder.append(java.lang.String)";
        put(STR_BUILD_APPEND_1, TaintCommandRunner.getInstance(STR_BUILD_APPEND_1, Command.APPEND));
    }};

    public static TaintCommandRunner getCommand(String signature) {
        return runnerMap.get(signature);
    }
}
