package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderReplaceTest {
    @Test
    public void testReplace() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();
        String src;
        StringBuilder obj;

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 3));
        src = "BAZ";
        obj = new StringBuilder("foobar");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("foBAZar", obj.toString());
        Assert.assertEquals("Taints:[untrusted(2,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("foobar");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("foBaZar", obj.toString());
        Assert.assertEquals("Taints:[untrusted(2,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(1, 5));
        srcTs = new TaintRanges(new TaintRange(0, 3));
        src = "BAZ";
        obj = new StringBuilder("fOOBAr");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("fOBAZAr", obj.toString());
        Assert.assertEquals("Taints:[untrusted(1,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(1, 5));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("fOOBAr");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("fOBaZAr", obj.toString());
        Assert.assertEquals("Taints:[untrusted(1,3), untrusted(4,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(0, 3));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("FOObar");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("FOBaZar", obj.toString());
        Assert.assertEquals("Taints:[untrusted(0,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(3, 6));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("fooBAR");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("foBaZAR", obj.toString());
        Assert.assertEquals("Taints:[untrusted(2,3), untrusted(4,7)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(5, 6));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("foobaR");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("foBaZaR", obj.toString());
        Assert.assertEquals("Taints:[untrusted(6,7), untrusted(2,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(5, 6));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("FoobaR");
        obj.replace(2, 4, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 4, 2);
        Assert.assertEquals("FoBaZaR", obj.toString());
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(6,7), untrusted(2,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(0, 1));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("Foobar");
        obj.replace(2, 10, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 10, 2);
        Assert.assertEquals("FoBaZ", obj.toString());
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(2,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(0, 4));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("FOOBar");
        obj.replace(2, 10, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 10, 2);
        Assert.assertEquals("FOBaZ", obj.toString());
        Assert.assertEquals("Taints:[untrusted(0,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(3, 6));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("fooBAR");
        obj.replace(2, 10, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 2, 10, 2);
        Assert.assertEquals("foBaZ", obj.toString());
        Assert.assertEquals("Taints:[untrusted(2,3), untrusted(4,5)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(3, 6));
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(2, 3));
        src = "BaZ";
        obj = new StringBuilder("fooBAR");
        obj.replace(1, 6, src);
        tb.replace(ts, obj, oldTs, src, srcTs, 1, 6, 2);
        Assert.assertEquals("fBaZ", obj.toString());
        Assert.assertEquals("Taints:[untrusted(1,2), untrusted(3,4)]", ts.toString());
    }
}
