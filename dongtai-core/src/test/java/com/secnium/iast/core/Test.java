package com.secnium.iast.core;

import java.util.concurrent.atomic.AtomicLong;

public class Test {

    private static final ThreadLocal<Long> responseTime = new ThreadLocal<Long>();

    public void execute(int count){
        for (int i = 0; i < count; i++) {
            responseTime.set(System.currentTimeMillis());
            String testLine = "response time:"+(System.currentTimeMillis()-responseTime.get())+"ms";
        }
    }

    @org.junit.Test
    public void test(){
        Long start = System.currentTimeMillis();
        execute(10000);
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

}
