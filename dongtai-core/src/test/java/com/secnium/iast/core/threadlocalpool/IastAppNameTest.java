package com.secnium.iast.core.threadlocalpool;

import org.junit.Test;

public class IastAppNameTest {
    public static IastAppName appName = new IastAppName();

    @Test
    public void testCreate() throws InterruptedException {
        // System.out.println("--> 测试性能 ，创建");
        for (int i = 0; i < 2; i++) { // 10000000
            Thread.sleep(10);
            appName.set(new String("abc"));
        }
    }
}
