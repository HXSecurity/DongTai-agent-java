package io.dongtai.iast.common.entity.performance.metrics;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 线程信息指标
 *
 * @author chenyi
 * @date 2022/3/1
 */
public class ThreadInfoMetrics implements Serializable {
    private static final long serialVersionUID = -3379730885923364839L;

    /**
     * 当前线程数
     */
    @SerializedName("threadCount")
    private Integer threadCount;
    /**
     * 峰值线程数
     */
    @SerializedName("peakThreadCount")
    private Integer peakThreadCount;
    /**
     * 守护线程数
     */
    @SerializedName("daemonThreadCount")
    private Integer daemonThreadCount;

    /**
     * DongTai线程数
     */
    @SerializedName("dongTaiThreadCount")
    private Integer dongTaiThreadCount;

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(Integer peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public Integer getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(Integer daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public void setDongTaiThreadCount(Integer dongTaiThreadCount) {
        this.dongTaiThreadCount = dongTaiThreadCount;
    }

    public Integer getDongTaiThreadCount() {
        return dongTaiThreadCount;
    }

    @Override
    public String toString() {
        return "threadCount = " + threadCount +
                " peakThreadCount = " + peakThreadCount +
                " daemonThreadCount = " + daemonThreadCount +
                " dongTaiThreadCount = " + dongTaiThreadCount ;
    }
}
