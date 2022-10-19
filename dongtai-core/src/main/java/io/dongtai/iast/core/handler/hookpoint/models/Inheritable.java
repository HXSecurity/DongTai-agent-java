package io.dongtai.iast.core.handler.hookpoint.models;

import io.dongtai.iast.core.utils.StringUtils;

public enum Inheritable {
    ALL("all"),
    SUBCLASS("true"),
    SELF("false"),
    ;

    private final String value;

    Inheritable(String value) {
        this.value = value;
    }

    public static Inheritable parse(String str) {
        if (!StringUtils.isEmpty(str)) {
            Inheritable[] values = values();
            for (Inheritable inheritable : values) {
                if (inheritable.value.equalsIgnoreCase(str)) {
                    return inheritable;
                }
            }
        }
        throw new IllegalArgumentException("invalid inheritable value " + str);
    }
}
