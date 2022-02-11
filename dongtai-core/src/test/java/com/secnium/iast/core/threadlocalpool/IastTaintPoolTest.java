package com.secnium.iast.core.threadlocalpool;

import io.dongtai.iast.core.utils.threadlocal.IastTaintPool;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class IastTaintPoolTest {
    @Test
    public void addSourceToPool() {
        IastTaintPool iastTaintPool = new IastTaintPool();
        HashSet<Object> newTest = new HashSet<Object>();
        iastTaintPool.set(new HashSet<Object>());
        HashMap<String, String[]> node = new HashMap<String, String[]>();

        node.put("1", new String[]{"2"});
        iastTaintPool.addToPool(node);
        newTest.add(node);

        node.put("2", new String[]{"3"});
        node.put("2", new String[]{"4"});
        iastTaintPool.addToPool(node);
        newTest.add(node);

        node.put("3", new String[]{"4"});
        iastTaintPool.addToPool(node);
        newTest.add(node);

        for (Object obj : newTest) {
            System.out.println("obj = " + obj);
        }

        System.out.println("node = " + node);

    }

    @Test
    public void testArrayList() {
        ArrayList<String> tokens = new ArrayList<String>();

    }
}