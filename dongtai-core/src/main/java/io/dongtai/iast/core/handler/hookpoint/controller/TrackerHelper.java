package io.dongtai.iast.core.handler.hookpoint.controller;

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
     * @since 1.2.0
     */
    private int dubboLevel = 0;

    public void enterTrack() {
        this.trackCounts++;
    }

    public void leaveTrack() {
        this.trackCounts--;
    }

    public void enterPropagation() {
        if (isEnterEntry()) {
            this.propagationDepth++;
        }
    }

    public void leavePropagation() {
        if (isEnterEntry()) {
            this.propagationDepth--;
        }
    }

    /**
     * 进入http、离开level且传播节点深度为1时，进行传播判断
     *
     * @return 布尔值，true，是第一层；false，非第一层
     */
    public boolean isFirstLevelPropagator() {
        return isEnterEntry() && this.sourceLevel == 0 && this.leaveSource == 1
                && this.propagationDepth == 1;
    }

    public void enterHttp() {
        this.enterHttp++;
    }

    public void leaveHttp() {
        this.enterHttp--;
    }

    public void enterSource() {
        if (isEnterEntry()) {
            this.sourceLevel++;
        }
    }

    public void leaveSource() {
        if (isEnterEntry()) {
            this.sourceLevel--;
            if (enterHttp > 0 && leaveSource == 0) {
                leaveSource = 1;
            }
        }
    }

    public boolean hasTrack() {
        return this.sourceLevel == 0 && this.trackCounts == 0;
    }

    public void enterSink() {
        if (isEnterEntry()) {
            this.sinkDepth++;
        }
    }

    public void leaveSink() {
        if (isEnterEntry()) {
            this.sinkDepth--;
        }
    }

    public boolean isFirstLevelSource() {
        return isEnterEntry() && this.sourceLevel == 1;
    }

    public boolean isFirstLevelHttp() {
        return this.enterHttp == 1;
    }

    public boolean isFirstLevelSink() {
        return isFirstLevel(this.sinkDepth);
    }

    public boolean hasTaintValue() {
        return isFirstLevel(1);
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
        return this.dubboLevel == 1;
    }

    public boolean isExitedDubbo() {
        return dubboLevel == 0;
    }

    private boolean isEnterEntry() {
        return enterHttp > 0 || dubboLevel > 0;
    }

    private boolean isFirstLevel(int targetLevel) {
        if (this.enterHttp > 0) {
            return this.sourceLevel == 0 && this.leaveSource == 1
                    && targetLevel == 1;
        }
        if (this.dubboLevel > 0) {
            return targetLevel == 1;
        }
        return false;
    }

}
