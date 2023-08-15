package io.dongtai.iast.core.handler.hookpoint.models.policy;

public enum PolicyNodeType {
    SOURCE(2, "source"),
    PROPAGATOR(1, "propagator"),
    VALIDATOR(3, "validator"),
    SINK(4, "sink"),
    ;

    private final int type;
    private final String name;

    PolicyNodeType(int type, String name) {
        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static PolicyNodeType get(Integer type) {
        if (type == null) {
            return null;
        }
        PolicyNodeType[] values = values();
        for (PolicyNodeType policyNodeType : values) {
            if (policyNodeType.type == type) {
                return policyNodeType;
            }
        }
        return null;
    }
}
