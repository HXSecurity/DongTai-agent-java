package io.dongtai.iast.common.scope;

import java.util.ArrayDeque;
import java.util.Deque;

public class PolicyScope {
    private int agentLevel;
    private int sourceLevel;
    private int propagatorLevel;
    private int propagatorSkipDepth;
    private int sinkLevel;
    private final Deque<String> sinkQueue = new ArrayDeque<>();
    private int ignoreInternalLevel;
    /**
     * over max method pool size
     */
    private boolean overCapacity;

    public void enterAgent() {
        this.agentLevel++;
    }

    public boolean inAgent() {
        return this.agentLevel > 0;
    }

    /* renamed from: d */
    public void leaveAgent() {
        this.agentLevel = decrement(this.agentLevel);
    }

    public void enterSource() {
        this.sourceLevel++;
    }

    public boolean isValidSource() {
        return this.agentLevel == 0
                && this.ignoreInternalLevel == 0 && !this.overCapacity
                && this.sourceLevel == 1;
    }

    public void leaveSource() {
        this.sourceLevel = decrement(this.sourceLevel);
    }

    public void enterPropagator(boolean skipScope) {
        this.propagatorLevel++;
        if (skipScope) {
            this.propagatorSkipDepth++;
        }
    }

    public boolean isValidPropagator() {
        return this.agentLevel == 0
                && this.ignoreInternalLevel == 0 && !this.overCapacity && this.sourceLevel == 0
                && (this.propagatorLevel == 1 || this.propagatorSkipDepth > 0);
    }

    public void leavePropagator(boolean skipScope) {
        this.propagatorLevel = decrement(this.propagatorLevel);
        if (skipScope) {
            this.propagatorSkipDepth = decrement(this.propagatorSkipDepth);
        }
    }

    public void enterSink() {
        this.sinkLevel++;
    }

    public boolean isValidSink() {
        return this.agentLevel == 0
                && this.ignoreInternalLevel == 0 && !this.overCapacity && this.sourceLevel == 0
                && this.sinkLevel > 0;
    }

    public void leaveSink() {
        this.sinkQueue.pop();
        this.sinkLevel = decrement(this.sinkLevel);
    }

    public void enterIgnoreInternal() {
        this.ignoreInternalLevel++;
    }

    public void leaveIgnoreInternal() {
        this.ignoreInternalLevel = decrement(this.ignoreInternalLevel);
    }

    public boolean isOverCapacity() {
        return this.overCapacity;
    }

    public void setOverCapacity(boolean overCapacity) {
        this.overCapacity = overCapacity;
    }

    private int decrement(int level) {
        if (level > 0) {
            return level - 1;
        }
        return 0;
    }

    public Deque<String> getSinkQueue() {
        return sinkQueue;
    }

    public void addSinkType(String sinkType) {
        this.sinkQueue.push(sinkType);
    }
}
