package io.dongtai.iast.common.state;

public enum State {
    // agent is running
    RUNNING(1),
    // agent is paused
    PAUSED(2),
    // agent(core) is uninstalled
    UNINSTALLED(3),
    // agent has exception
    EXCEPTION(4),
    ;

    private final int code;

    State(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public boolean equals(String state) {
        try {
            int c = Integer.valueOf(state);
            return c == this.code;
        } catch (Throwable ignore) {
            return false;
        }
    }
}
