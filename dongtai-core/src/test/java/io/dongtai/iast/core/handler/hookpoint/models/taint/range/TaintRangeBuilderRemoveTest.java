package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TaintRangeBuilderRemoveTest {
    @Test
    public void testRemoveReset() throws IOException {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();
        ByteArrayOutputStream baos;

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 6));
        // new ByteArrayOutputStream(6).write("FOOBAR".getBytes()).reset()
        baos = new ByteArrayOutputStream(6);
        baos.write("FOOBAR".getBytes());
        tb.remove(ts, baos, srcTs, 0, 0, 0);
        Assert.assertEquals("Taints:[]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 6));
        // new ByteArrayOutputStream(6).write("fooBAR".getBytes()).reset()
        baos = new ByteArrayOutputStream(6);
        baos.write("fooBAR".getBytes());
        tb.remove(ts, baos, srcTs, 0, 0, 0);
        Assert.assertEquals("Taints:[]", ts.toString());
    }

    @Test
    public void testRemoveCharAt() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 7));
        // new StringBuilder("FOOZBAR").deleteCharAt(3)     // FOOBAR
        tb.remove(ts, new StringBuilder("FOOBAR"), srcTs, 3, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1, 6));
        // new StringBuilder("fOOZBAr").deleteCharAt(3)    // fOOBAr
        tb.remove(ts, new StringBuilder("fOOBAr"), srcTs, 3, 0, 1);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 3), new TaintRange(4, 7));
        // new StringBuilder("FOOzBAR").deleteCharAt(3)    // FOOBAR
        tb.remove(ts, new StringBuilder("FOOBAR"), srcTs, 3, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());
    }

    @Test
    public void testRemoveStartStop() {
        TaintRanges ts;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(0, 9));
        // new StringBuilder("FOOZZZBAR").delete(3, 6)   // FOOBAR
        tb.remove(ts, new StringBuilder("FOOBAR"), tgtTs, 3, 6, 2);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(1, 8));
        // new StringBuilder("fOOZZZBAr").delete(3, 6)  // fOOBAr
        tb.remove(ts, new StringBuilder("fOOBAr"), tgtTs, 3, 6, 2);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(0, 3), new TaintRange(4, 5), new TaintRange(6, 9));
        // new StringBuilder("FOOzZzBAR").delete(3, 6)  // FOOBAR
        tb.remove(ts, new StringBuilder("FOOBAR"), tgtTs, 3, 6, 2);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());
    }
}
