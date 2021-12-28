package com.secnium.iast.core.handler.controller;

import com.secnium.iast.core.EngineManager;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TrackerHelper {

    private int trackCounts = 0;
    private int propagationDepth = 0;
    private int sourceLevel = 0;
    private int enterHttp = 0;
    private int leaveSource = 0;
    private int sinkDepth = 0;
    /**
     * @since 1.1.4
     */
    private int dubboLevel = 0;

    public void enterTrack() {
        this.trackCounts++;
    }

    public void leaveTrack() {
        this.trackCounts--;
    }

    public void enterPropagation() {
        this.propagationDepth++;
    }

    public void leavePropagation() {
        this.propagationDepth--;
    }

    /**
     * 进入http、离开level且传播节点深度为1时，进行传播判断
     *
     * @return 布尔值，true，是第一层；false，非第一层
     */
    public boolean isFirstLevelPropagator() {
        return isEnableLingzhi() && this.enterHttp > 0 && this.sourceLevel == 0 && this.leaveSource == 1
                && this.propagationDepth == 1;
    }

    public void enterHttp() {
        this.enterHttp++;
    }

    public void leaveHttp() {
        this.enterHttp--;
    }

    public void enterSource() {
        this.sourceLevel++;
    }

    public void leaveSource() {
        this.sourceLevel--;
        if (enterHttp > 0 && leaveSource == 0) {
            leaveSource = 1;
        }
    }

    public boolean hasTrack() {
        return this.sourceLevel == 0 && this.trackCounts == 0;
    }

    public void enterSink() {
        this.sinkDepth++;
    }

    public void leaveSink() {
        this.sinkDepth--;
    }

    public boolean isFirstLevelSource() {
        return isEnableLingzhi() && this.enterHttp > 0 && this.sourceLevel == 1;
    }

    public boolean isFirstLevelHttp() {
        return isEnableLingzhi() && this.enterHttp == 1;
    }

    public boolean isFirstLevelSink() {
        return isEnableLingzhi() && this.enterHttp > 0 && this.sourceLevel == 0 && this.leaveSource == 1
                && this.sinkDepth == 1;
    }

    public boolean hasTaintValue() {
        return isEnableLingzhi() && this.enterHttp > 0 && this.sourceLevel == 0 && this.leaveSource == 1;
    }

    public boolean isEnableLingzhi() {
        return EngineManager.isEngineEnable();
    }

    public boolean isExitedHttp() {
        return this.enterHttp == 0;
    }

    public void enterDubbo() {
        this.dubboLevel++;
    }

    public void leaveDubbo() {
        this.dubboLevel--;
    }

    public boolean isFirstLevelDubbo() {
        return isEnableLingzhi() && this.dubboLevel == 1;
    }

    @Override
    public String toString() {
        return "TrackerHelper{" +
                "trackCounts=" + trackCounts +
                ", propagationDepth=" + propagationDepth +
                ", sourceLevel=" + sourceLevel +
                ", enterHttp=" + enterHttp +
                ", leaveSource=" + leaveSource +
                ", sinkDepth=" + sinkDepth +
                '}';
    }

}
