package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderKeepTest {
    @Test
    public void testKeep() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 6));
        // new StringBuilder("FOOBAR")
        tb.keep(ts, new StringBuilder("FOOBAR"), 0, srcTs);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 5));
        // new StringBuilder("fooBAr")
        tb.keep(ts, new StringBuilder("fooBAr"), 0, srcTs);
        Assert.assertEquals("Taints:[untrusted(3,5)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 1), new TaintRange(3, 5));
        // new StringBuilder("FooBAr")
        tb.keep(ts, new StringBuilder("FooBAr"), 0, srcTs);
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(3,5)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1, 3), new TaintRange(3, 5));
        // new StringBuilder("fOOBAr")
        tb.keep(ts, new StringBuilder("fOOBAr"), 0, srcTs);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }
}
