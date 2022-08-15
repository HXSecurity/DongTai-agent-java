package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderKeepTest {
    @Test
    public void testKeep() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(0,6));
        // new StringBuilder("foobar")
        tb.keep(ts, new StringBuilder("foobar"), 0, tgtTs);
        Assert.assertEquals("Taints:[untrusted(0,6)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,5));
        // new StringBuilder("foobar")
        tb.keep(ts, new StringBuilder("foobar"), 0, tgtTs);
        Assert.assertEquals("Taints:[untrusted(3,5)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(0,1), new TaintRange(3,5));
        // new StringBuilder("foobar")
        tb.keep(ts, new StringBuilder("foobar"), 0, tgtTs);
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(3,5)]", ts.toString());
    }
}
