package com.secnium.iast.core;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;
import org.junit.Test;

import java.util.HashMap;

public class AgentEngineTest {

    public static void main(String[] args) {
        System.out.println(System.getProperty("os.arch"));
    }

    public AgentEngineTest() {

    }

    @Test
    public void a() {
        BooleanThreadLocal booleanThreadLocal = new BooleanThreadLocal(false);
        System.out.println(booleanThreadLocal.isEnterEntry());
        booleanThreadLocal.set(true);
        System.out.println(booleanThreadLocal.isEnterEntry());
        booleanThreadLocal.remove();
        System.out.println(booleanThreadLocal.isEnterEntry());
    }

    @Test
    public void b(boolean a) {
        try {
            if (a){
                try {
                    throw new IllegalStateException("DongTai agent request replay");
                }catch (RuntimeException e){
                    System.out.println("DongTai agent request replay, please ignore");
                }
            }
        } catch (NullPointerException e) {
            DongTaiLog.info("DongTai agent request replay, please ignore");
        }
    }

    @Test
    public void c() {

    }


}
