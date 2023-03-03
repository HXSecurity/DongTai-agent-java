package io.dongtai.iast.common.scope;

public enum Scope {
    HTTP_REQUEST(1),
    HTTP_ENTRY(2),
    SERVLET_INPUT_STREAM_READ(3),
    SERVLET_OUTPUT_WRITE(4),
    DUBBO_REQUEST(5),
    DUBBO_ENTRY(6),
    DUBBO_SOURCE(7),
    ;

    private final int id;

    Scope(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static Scope getScope(int id) {
        for (Scope each : Scope.values()) {
            if (id == each.id) {
                return each;
            }
        }

        return null;
    }
}
