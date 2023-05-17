package io.dongtai.iast.common.state;

public class AgentState {
    private State state;
    private State pendingState;
    private StateCause cause;
    private boolean fallback;
    private boolean allowReport = true;
    private static AgentState INSTANCE;

    public static AgentState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AgentState();
        }
        return INSTANCE;
    }

    public State getState() {
        return this.state;
    }

    public AgentState setState(State state) {
        this.state = state;
        return this;
    }

    public State getPendingState() {
        return this.pendingState;
    }

    public String getPendingStateInfo() {
        if (this.pendingState == State.RUNNING) {
            return "currently in the progress of installation";
        } else if (this.pendingState == State.UNINSTALLED) {
            return "currently in the progress of uninstallation";
        }
        return "currently in progress";
    }

    public AgentState setPendingState(State pendingState) {
        this.pendingState = pendingState;
        return this;
    }

    public StateCause getCause() {
        return this.cause;
    }

    public AgentState setCause(StateCause cause) {
        this.cause = cause;
        return this;
    }

    public boolean isRunning() {
        return this.state == State.RUNNING;
    }

    public boolean isPaused() {
        return this.state == State.PAUSED;
    }

    public boolean isInit() {
        return this.state != null;
    }

    public boolean isUninstalled() {
        return this.state == State.UNINSTALLED;
    }

    public boolean isUninstalledByCli() {
        return isUninstalled() && this.cause == StateCause.UNINSTALL_BY_CLI;
    }

    public boolean isException() {
        return this.state == State.EXCEPTION;
    }

    public void fallbackToPause() {
        this.state = State.PAUSED;
        this.cause = StateCause.PAUSE_BY_FALLBACK;
        this.fallback = true;
    }

    public void fallbackToUninstall() {
        this.state = State.UNINSTALLED;
        this.cause = StateCause.UNINSTALL_BY_FALLBACK;
        this.fallback = true;
    }

    public void fallbackRecover() {
        this.state = State.RUNNING;
        this.cause = StateCause.RUNNING_BY_FALLBACK_RECOVER;
        this.fallback = true;
    }

    public boolean isFallback() {
        return this.fallback;
    }

    public boolean isAllowReport() {
        return this.allowReport;
    }

    public void setAllowReport(boolean allowReport) {
        this.allowReport = allowReport;
    }
}
