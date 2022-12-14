package io.dongtai.iast.common.state;

/**
 * agent state action/cause
 */
public enum StateCause {
    // running by command line
    RUNNING_BY_CLI(1),
    // running after fallback recover
    RUNNING_BY_FALLBACK_RECOVER(2),
    // running by server expect state
    RUNNING_BY_SERVER(3),
    // pause after fallback
    PAUSE_BY_FALLBACK(4),
    // pause by server expect state
    PAUSE_BY_SERVER(5),
    // uninstall by command line
    UNINSTALL_BY_CLI(6),
    // uninstall after fallback
    UNINSTALL_BY_FALLBACK(7),
    ;

    private final int code;

    public int getCode() {
        return this.code;
    }

    StateCause(int code) {
        this.code = code;
    }
}
