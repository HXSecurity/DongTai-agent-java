package com.secnium.iast.core.threadlocalpool;

import com.secnium.iast.core.handler.controller.TrackerHelper;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IASTScopeTracker extends ThreadLocal<TrackerHelper> {
    @Override
    protected TrackerHelper initialValue() {
        return new TrackerHelper();
    }

    public void enterHttp() {
        this.get().enterHttp();
    }

    public void leaveHttp() {
        this.get().leaveHttp();
    }

    public boolean isFirstLevelHttp() {
        return this.get().isFirstLevelHttp();
    }

    public boolean isExitedHttp(){
        return this.get().isExitedHttp();
    }

    public void enterSink() {
        this.get().enterSink();
    }

    public void leaveSink() {
        this.get().leaveSink();
    }

    public boolean isFirstLevelSink() {
        return this.get().isFirstLevelSink();
    }

    public void enterSource() {
        this.get().enterSource();
    }

    public void leaveSource() {
        this.get().leaveSource();
    }

    public boolean isFirstLevelSource() {
        return this.get().isFirstLevelSource();
    }

    public void enterPropagation() {
        this.get().enterPropagation();
    }

    public void leavePropagation() {
        this.get().leavePropagation();
    }

    public boolean isFirstLevelPropagator() {
        return this.get().isFirstLevelPropagator();
    }

    public boolean hasTaintValue() {
        return this.get().hasTaintValue();
    }
}
