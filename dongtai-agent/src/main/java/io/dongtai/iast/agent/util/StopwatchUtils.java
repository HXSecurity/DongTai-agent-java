package io.dongtai.iast.agent.util;

import org.apache.commons.lang3.time.StopWatch;

/**
 * 秒表工具类
 *
 * @author liyuan40
 * @date 2022/3/28 11:34
 */
public class StopwatchUtils {

    public static void start(StopWatch stopWatch) {
        try {
            stopWatch.start();
        } catch (Throwable e) {
            // DongTaiLog.info("Stopwatch.start() method invoke exception.", Arrays.toString(e.getStackTrace()));
        }
    }

    public static void stop(StopWatch stopWatch) {
        try {
            stopWatch.stop();
        } catch (Throwable e) {
            // DongTaiLog.info("Stopwatch.stop() method invoke exception.", Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 获取秒表的持续时间
     */
    public static Long getTime(StopWatch stopWatch) {
        try {
            return stopWatch.getTime();
        } catch (Throwable e) {
            // DongTaiLog.info("Stopwatch.getTime() method invoke exception.", Arrays.toString(e.getStackTrace()));
            return 0L;
        }
    }

    /**
     * 获取开始时间
     */
    public static Long getStartTime(StopWatch stopWatch) {
        try {
            return stopWatch.getStartTime();
        } catch (Throwable e) {
            // DongTaiLog.info("Stopwatch.getStartTime() method invoke exception.", Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

}
