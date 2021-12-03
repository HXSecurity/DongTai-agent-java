package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

/**
 * @author owefsad
 */
public class ReportThread extends Thread {

    private final Logger logger = LogUtils.getLogger(ReportThread.class);
    private final String report;
    private final String uri;

    public ReportThread(String uri, String report) {
        this.uri = uri;
        this.report = report;
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     * @see #Thread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        boolean isRunning = EngineManager.isLingzhiRunning();
        if (isRunning) {
            EngineManager.turnOffLingzhi();
        }
        try {
            HttpClientUtils.sendPost(uri, report);
        } catch (Exception e) {
            logger.error("report error, reason: ", e);
        }
        if (isRunning) {
            EngineManager.turnOnLingzhi();
        }
    }

    public static void send(String url, String report) {
        ReportThread reportThread = new ReportThread(url, report);
        reportThread.start();
    }
}
