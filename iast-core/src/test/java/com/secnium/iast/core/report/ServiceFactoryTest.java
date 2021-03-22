package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.ServiceFactory;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServiceFactoryTest {
    @Test
    public void init() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils.getInstance(propertiesFilePath);
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        assert null != serviceFactory;
        serviceFactory.init();
    }

    @Test
    public void start() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils.getInstance(propertiesFilePath);
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        assert null != serviceFactory;
        serviceFactory.init();
        serviceFactory.start();

        int times = 0;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                times++;
            }
        } while (times < 60);
    }

    @Test
    public void destory() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils.getInstance(propertiesFilePath);
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        assert null != serviceFactory;
        serviceFactory.init();
        serviceFactory.start();

        int times = 0;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                times++;
            }
        } while (times < 30);

        serviceFactory.destory();
    }

    @Test
    public void scheduleTask() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//        executorService.scheduleWithFixedDelay(new HeartBeatReport(), 30, 2, TimeUnit.SECONDS);
//        executorService.scheduleWithFixedDelay(new VulnReport(), 30, 2, TimeUnit.SECONDS);
//        executorService.scheduleWithFixedDelay(new AssestReport(), 30, 2, TimeUnit.SECONDS);
//        executorService.scheduleWithFixedDelay(new ErrorLogReport(), 30, 2, TimeUnit.SECONDS);
    }
}
