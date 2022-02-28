package io.dongtai.iast.core.service;

import io.dongtai.iast.core.replay.HttpRequestReplay;
import io.dongtai.iast.core.utils.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author owefsad
 */
public class ThreadPools {

    private static final ExecutorService METHOD_REPORT_THREAD = new ThreadPoolExecutor(0, 5, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(5120), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, Constants.THREAD_NAME_PREFIX + "VulReport-" + r.hashCode());
        }
    });

    private static final ExecutorService SCA_REPORT_THREAD = new ThreadPoolExecutor(0, 5, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, Constants.THREAD_NAME_PREFIX + "Report-" + r.hashCode());
        }
    });

    private static final ExecutorService REPLAY_REQUEST_THREAD = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1024), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, Constants.THREAD_NAME_PREFIX + "VulReplay-" + r.hashCode());
        }

    });

    private static final ExecutorService LIMIT_REPORT_THREAD = new ThreadPoolExecutor(0, 5, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(5120), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, Constants.THREAD_NAME_PREFIX + "LimitReport-" + r.hashCode());
        }
    });


    public static void execute(Runnable r) {
        SCA_REPORT_THREAD.execute(r);
    }

    public static void sendPriorityReport(final String url, final String report) {
        METHOD_REPORT_THREAD.execute(new ReportThread(url, report));
    }

    public static void sendReport(final String url, final String report) {
        SCA_REPORT_THREAD.execute(new ReportThread(url, report));
    }

    public static void submitReplayTask(StringBuilder replayRequestRaw) {
        REPLAY_REQUEST_THREAD.execute(new HttpRequestReplay(replayRequestRaw));
    }

    public static void sendLimitReport(final String url, final String report) {
        LIMIT_REPORT_THREAD.execute(new ReportThread(url, report));
    }
}
