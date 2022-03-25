package com.secnium.iast.core;

import io.dongtai.log.DongTaiLog;

import java.util.concurrent.TimeUnit;

public class AgentEngineTest {

    public static void main(String[] args) {
        new AgentEngineTest().a();
    }

    public AgentEngineTest(){

    }

    public void a(){
        try {
            System.out.println("b");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("a");
        } catch (InterruptedException e) {
            DongTaiLog.error(e);
        }
    }

}
