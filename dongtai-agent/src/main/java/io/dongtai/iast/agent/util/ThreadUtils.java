package io.dongtai.iast.agent.util;

import io.dongtai.log.DongTaiLog;

import java.util.HashSet;
import java.util.Set;

public class ThreadUtils {
    /**
     * 获取所有名称中包含DongTai字样的线程
     * @return
     */
    public static Set<String> getDongTaiThreads(){
        Set<String> threadSet = new HashSet<String>();
        try{
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int threadNum = currentGroup.activeCount();
            Thread[] threads = new Thread[threadNum];
            currentGroup.enumerate(threads);
            for (int i = 0; i < threadNum; i++) {
                // 匹配DongTai线程
                if(threads[i].getName().contains("DongTai")){
                    threadSet.add(threads[i].getName());
                }
            }
        }catch (Throwable t){
            DongTaiLog.warn("Get DongTai thread failed, msg: {} , error: {}",t.getMessage(),t.getCause());
        }
        return threadSet;
    }
    public static void threadSleep(int seconds) {
        try {
            long milliseconds = seconds * 1000L;
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
