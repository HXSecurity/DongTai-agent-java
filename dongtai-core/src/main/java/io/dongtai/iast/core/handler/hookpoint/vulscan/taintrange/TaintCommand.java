package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

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
}
