package io.dongtai.iast.common.entity.performance.metrics;


import java.io.Serializable;
import java.util.List;

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
    private Integer threadCount;
    /**
     * 峰值线程数
     */
    private Integer peakThreadCount;
    /**
     * 守护线程数
     */
    private Integer daemonThreadCount;
    /**
     * 洞态线程数
     */
    private Integer dongTaiThreadCount;
    /**
     * 洞态线程信息列表
     */
    private List<ThreadInfo> dongTaiThreadInfoList;

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

    public List<ThreadInfo> getDongTaiThreadInfoList() {
        return dongTaiThreadInfoList;
    }

    public void setDongTaiThreadInfoList(List<ThreadInfo> dongTaiThreadInfoList) {
        this.dongTaiThreadInfoList = dongTaiThreadInfoList;
        if (dongTaiThreadInfoList != null) {
            this.dongTaiThreadCount = dongTaiThreadInfoList.size();
        }
    }

    @Override
    public String toString() {
        return "threadCount = " + threadCount +
                " peakThreadCount = " + peakThreadCount +
                " daemonThreadCount = " + daemonThreadCount +
                " dongTaiThreadCount = " + dongTaiThreadCount ;
    }


    /**
     * 线程信息
     *
     * @author chenyi
     * @date 2022/03/14
     */
    public static class ThreadInfo implements Serializable {
        private static final long serialVersionUID = -6373463116911707950L;
        /**
         * 线程id
         */
        private Long id;
        /**
         * 线程名
         */
        private String name;
        /**
         * 线程cpu时间
         */
        private Long cpuTime;
        /**
         * cpu时间收集时间点(纳秒)
         */
        private Long cpuTimeCollectNanos;
        /**
         * 线程cpu使用率
         */
        private Double cpuUsage;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCpuTime() {
            return cpuTime;
        }

        public void setCpuTime(Long thisCpuTime) {
            // 本次cpu时间和采集时间点
            Long thisCpuTimeCollectNanos = System.nanoTime();
            // 上次cpu时间和采集时间点
            Long lastCpuTime = this.cpuTime != null ? this.cpuTime : 0L;
            Long lastCpuTimeCollectNanos = this.cpuTimeCollectNanos != null ? this.cpuTimeCollectNanos : thisCpuTimeCollectNanos;
            // 设置cpu时间、收集时间点
            this.cpuTime = thisCpuTime;
            this.cpuTimeCollectNanos = thisCpuTimeCollectNanos;
            // 根据两次采集间隔，计算cpu使用率
            this.cpuUsage = calculateCpuUsage(lastCpuTime, lastCpuTimeCollectNanos, thisCpuTime, thisCpuTimeCollectNanos);
        }

        public Double getCpuUsage() {
            return cpuUsage;
        }

        /**
         * 计算cpu使用率
         *
         * @param lastCpuTime             上次记录的cpu时间
         * @param lastCpuTimeCollectNanos 上次cpu时间收集时间点(纳秒)
         * @param thisCpuTime             本次记录的cpu时间
         * @param thisCpuTimeCollectNanos 本次cpu时间收集时间点(纳秒)
         * @return double cpu使用率
         */
        private double calculateCpuUsage(Long lastCpuTime, Long lastCpuTimeCollectNanos, Long thisCpuTime, Long thisCpuTimeCollectNanos) {
            if (lastCpuTime == -1) {
                lastCpuTime = thisCpuTime;
            } else if (thisCpuTime == -1) {
                thisCpuTime = lastCpuTime;
            }
            long delta = thisCpuTime - lastCpuTime;
            long collectIntervalNanos = thisCpuTimeCollectNanos - lastCpuTimeCollectNanos;
            return collectIntervalNanos == 0 ? 0 : (delta * 10000.0 / collectIntervalNanos / 100.0);
        }
    }
}
