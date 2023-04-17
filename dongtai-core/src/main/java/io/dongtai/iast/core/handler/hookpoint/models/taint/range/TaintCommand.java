package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import io.dongtai.iast.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum TaintCommand {
    KEEP,
    APPEND,
    SUBSET,
    INSERT,
    REMOVE,
    REPLACE,
    CONCAT,
    TRIM,
    TRIM_RIGHT,
    TRIM_LEFT,
    ;

    private static final Map<String, TaintCommand> LOOKUP = new HashMap<String, TaintCommand>();

    static {
        for (TaintCommand t : TaintCommand.values()) {
            LOOKUP.put(t.name(), t);
        }
    }

    public static TaintCommand get(String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            return null;
        }
        cmd = cmd.toUpperCase();
        return LOOKUP.get(cmd);
    }
}
