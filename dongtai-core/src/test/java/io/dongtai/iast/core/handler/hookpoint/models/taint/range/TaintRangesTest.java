package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.*;

public class TaintRangesTest {
    @Test
    public void testShift() {
        TaintRange tr1;
        TaintRange tr2;
        TaintRanges ts;

        tr1 = new TaintRange(5, 9);
        tr2 = new TaintRange(13, 16);
        ts = new TaintRanges(tr1, tr2);
        ts.shift(3);
        Assert.assertEquals(ts.toString(), "Taints:[untrusted(8,12), untrusted(16,19)]");

        tr1 = new TaintRange(5, 9);
        tr2 = new TaintRange(13, 16);
        ts = new TaintRanges(tr1, tr2);
        ts.shift(-3);
        Assert.assertEquals(ts.toString(), "Taints:[untrusted(2,6), untrusted(10,13)]");

        tr1 = new TaintRange(5, 9);
        tr2 = new TaintRange(13, 16);
        final TaintRanges fts = new TaintRanges(tr1, tr2);
        Assert.assertThrows(RuntimeException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                fts.shift(-6);
            }
        });
    }

    @Test
    public void testTrim() {
        TaintRange tr;
        TaintRanges ts;

        // base: TaintRange(5, 10)
        Map<List<Integer>, String> tests = new HashMap<List<Integer>, String>() {{
            put(Arrays.asList(7, 7), "Taints:[]");
            //      |---|
            //    |-|
            put(Arrays.asList(3, 5), "Taints:[]");
            // |----|
            //   |----|
            put(Arrays.asList(7, 12), "Taints:[untrusted(0,3)]");
            //      |----|
            //    |----|
            put(Arrays.asList(3, 8), "Taints:[untrusted(2,5)]");
            //      |----|
            //     |------|
            put(Arrays.asList(4, 11), "Taints:[untrusted(1,6)]");
            //      |----|
            //       |-|
            put(Arrays.asList(6, 8), "Taints:[untrusted(0,2)]");
            //    |----|
            //      |---|
            put(Arrays.asList(7, 12), "Taints:[untrusted(0,3)]");
            //  |----|
            //       |-|
            put(Arrays.asList(10, 12), "Taints:[]");
        }};

        for (Map.Entry<List<Integer>, String> entry : tests.entrySet()) {
            tr = new TaintRange(5, 10);
            ts = new TaintRanges(tr);
            ts.trim(entry.getKey().get(0), entry.getKey().get(1));
            Assert.assertEquals(tr.toString() + " | " + entry.getKey().toString(), entry.getValue(), ts.toString());
        }
    }

    @Test
    public void testSplit() {
        Map<TaintRange, String> tests = new HashMap<TaintRange, String>() {{
            put(new TaintRange(1, 4), "Taints:[untrusted(8,13)]");
            put(new TaintRange(1, 5), "Taints:[untrusted(9,14)]");
            put(new TaintRange(1, 7), "Taints:[untrusted(11,16)]");
            put(new TaintRange(1, 11), "Taints:[untrusted(15,20)]");
            put(new TaintRange(6, 7), "Taints:[untrusted(5,6), untrusted(7,11)]");
            put(new TaintRange(6, 11), "Taints:[untrusted(5,6), untrusted(11,15)]");
            put(new TaintRange(5, 15), "Taints:[untrusted(15,20)]");
            put(new TaintRange(10, 15), "Taints:[untrusted(5,10)]");
            put(new TaintRange(11, 15), "Taints:[untrusted(5,10)]");
        }};

        for (Map.Entry<TaintRange, String> entry : tests.entrySet()) {
            TaintRanges ts = new TaintRanges(new TaintRange(5, 10));
            ts.split(entry.getKey().start, entry.getKey().stop);
            Assert.assertEquals(entry.getKey().toString(), entry.getValue(), ts.toString());
        }
    }

    @Test
    public void testSubRange() {
        Map<TaintRange, String> tests = new HashMap<TaintRange, String>() {{
            put(new TaintRange(1, 4), "Taints:[]");
            put(new TaintRange(1, 5), "Taints:[]");
            put(new TaintRange(1, 7), "Taints:[untrusted(5,7)]");
            put(new TaintRange(1, 11), "Taints:[untrusted(5,10)]");
            put(new TaintRange(6, 7), "Taints:[untrusted(6,7)]");
            put(new TaintRange(6, 11), "Taints:[untrusted(6,10)]");
            put(new TaintRange(5, 15), "Taints:[untrusted(5,10)]");
            put(new TaintRange(10, 15), "Taints:[]");
            put(new TaintRange(11, 15), "Taints:[]");
        }};

        for (Map.Entry<TaintRange, String> entry : tests.entrySet()) {
            TaintRanges ts = new TaintRanges(new TaintRange(5, 10));
            ts.subRange(entry.getKey().start, entry.getKey().stop);
            Assert.assertEquals(entry.getKey().toString(), entry.getValue(), ts.toString());
        }
    }

    @Test
    public void testRemove() {
        Map<TaintRange, String> tests = new HashMap<TaintRange, String>() {{
            put(new TaintRange(1, 4), "Taints:[untrusted(2,7)]");
            put(new TaintRange(1, 5), "Taints:[untrusted(1,6)]");
            put(new TaintRange(1, 7), "Taints:[untrusted(1,4)]");
            put(new TaintRange(1, 11), "Taints:[]");
            put(new TaintRange(6, 7), "Taints:[untrusted(5,9)]");
            put(new TaintRange(6, 11), "Taints:[untrusted(5,6)]");
            put(new TaintRange(5, 15), "Taints:[]");
            put(new TaintRange(10, 15), "Taints:[untrusted(5,10)]");
            put(new TaintRange(11, 15), "Taints:[untrusted(5,10)]");
        }};

        for (Map.Entry<TaintRange, String> entry : tests.entrySet()) {
            TaintRanges ts = new TaintRanges(new TaintRange(5, 10));
            ts.remove(entry.getKey().start, entry.getKey().stop);
            Assert.assertEquals(entry.getKey().toString(), entry.getValue(), ts.toString());
        }
    }

    @Test
    public void testClear() {
        Map<TaintRange, String> tests = new HashMap<TaintRange, String>() {{
            put(new TaintRange(1, 4), "Taints:[untrusted(5,10)]");
            put(new TaintRange(1, 5), "Taints:[untrusted(5,10)]");
            put(new TaintRange(1, 7), "Taints:[untrusted(7,10)]");
            put(new TaintRange(1, 11), "Taints:[]");
            put(new TaintRange(6, 7), "Taints:[untrusted(5,6), untrusted(7,10)]");
            put(new TaintRange(6, 11), "Taints:[untrusted(5,6)]");
            put(new TaintRange(5, 15), "Taints:[]");
            put(new TaintRange(10, 15), "Taints:[untrusted(5,10)]");
            put(new TaintRange(11, 15), "Taints:[untrusted(5,10)]");
        }};

        for (Map.Entry<TaintRange, String> entry : tests.entrySet()) {
            TaintRanges ts = new TaintRanges(new TaintRange(5, 10));
            ts.clear(entry.getKey().start, entry.getKey().stop);
            Assert.assertEquals(entry.getKey().toString(), entry.getValue(), ts.toString());
        }
    }

    @Test
    public void testMerge() {
        TaintRange tr1;
        TaintRange tr2;
        TaintRanges ts;

        tr1 = new TaintRange(5, 10);
        Map<TaintRange, String> tests = new HashMap<TaintRange, String>() {{
            put(new TaintRange(1, 4), "Taints:[untrusted(5,10), untrusted(1,4)]");
            put(new TaintRange(1, 5), "Taints:[untrusted(1,10)]");
            put(new TaintRange(1, 7), "Taints:[untrusted(1,10)]");
            put(new TaintRange(1, 11), "Taints:[untrusted(1,11)]");
            put(new TaintRange(6, 7), "Taints:[untrusted(5,10)]");
            put(new TaintRange(6, 11), "Taints:[untrusted(5,11)]");
            put(new TaintRange(5, 15), "Taints:[untrusted(5,15)]");
            put(new TaintRange(10, 15), "Taints:[untrusted(5,15)]");
            put(new TaintRange(11, 15), "Taints:[untrusted(5,10), untrusted(11,15)]");
        }};

        for (Map.Entry<TaintRange, String> entry : tests.entrySet()) {
            tr2 = entry.getKey();
            ts = new TaintRanges(tr1, tr2);
            TaintRanges tsCopy = ts;
            ts.merge();
            Assert.assertEquals(tsCopy.toString(), entry.getValue(), ts.toString());
        }
    }
}
