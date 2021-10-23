package com.secnium.iast.core;


import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * 抽象线程类，直接继承Runnable接口，用于发送报告、重放请求
 *
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractThread extends Thread {
    private final Logger logger = LogUtils.getLogger(getClass());

    @Override
    public void run() {
        boolean isRunning = EngineManager.isLingzhiRunning();
        if (isRunning) {
            EngineManager.turnOffLingzhi();
        }
        try {
            send();
        } catch (IOException e) {
            logger.error("report error reason: ", e);
        } catch (Exception e) {
            logger.error("report error, reason: ", e);
        }
        if (isRunning) {
            EngineManager.turnOnLingzhi();
        }
    }

    /**
     * 发送请求
     *
     * @throws Exception 捕获发送报告过程中出现的异常，用于兜底，避免发送报告线程未正确处理导致异常退出
     */
    protected abstract void send() throws Exception;
}
