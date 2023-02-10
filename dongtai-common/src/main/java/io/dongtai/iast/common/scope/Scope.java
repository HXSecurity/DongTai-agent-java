package io.dongtai.iast.common.scope;

public enum Scope {
    HTTP_REQUEST(1),
    HTTP_ENTRY(2),
    HTTP_RESPONSE_HEADER(3),
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
