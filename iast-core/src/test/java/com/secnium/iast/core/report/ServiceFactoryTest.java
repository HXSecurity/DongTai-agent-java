package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.ServiceFactory;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServiceFactoryTest {
    @Test
    public void init() {
        try {
            String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
            PropertyUtils.getInstance(propertiesFilePath);
            ServiceFactory serviceFactory = ServiceFactory.getInstance();
            assert null != serviceFactory;
            serviceFactory.init();
        } catch (Exception e) {
            System.err.println(" ServiceFactoryTest  error  init " + e.getMessage());
        }
    }

    @Test
    public void start() {
        try {
            String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
            PropertyUtils.getInstance(propertiesFilePath);
            ServiceFactory serviceFactory = ServiceFactory.getInstance();
            assert null != serviceFactory;
            serviceFactory.init();
            serviceFactory.start();
        } catch (Exception e) {
            System.err.println("--- < start error " + e.getMessage());
            return;
        }

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
        ServiceFactory serviceFactory = null;
        try {
            String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
            PropertyUtils.getInstance(propertiesFilePath);
            serviceFactory = ServiceFactory.getInstance();
            assert null != serviceFactory;
            serviceFactory.init();
            serviceFactory.start();
        } catch (Exception e) {
            System.err.println("  destory error " + e.getMessage());
            return;
        }

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
        executorService.scheduleWithFixedDelay(new ReportSender(), 30, 2, TimeUnit.SECONDS);
    }
}
