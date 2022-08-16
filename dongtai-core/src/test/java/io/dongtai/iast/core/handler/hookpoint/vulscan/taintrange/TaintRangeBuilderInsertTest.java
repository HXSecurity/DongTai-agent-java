package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderInsertTest {
    @Test
    public void testInsert() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(0,2));
        // new StringBuilder("fbar").insert(1, "OO")    // fOObar
        tb.insert(ts, srcTs, new StringBuilder("OO"), tgtTs, 1, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(2,3));
        tgtTs = new TaintRanges(new TaintRange(0,2));
        // new StringBuilder("fbAr").insert(1, "OO")    // fOObAr
        tb.insert(ts, srcTs, new StringBuilder("OO"), tgtTs, 1, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(4,5), untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0,1), new TaintRange(2,3));
        tgtTs = new TaintRanges(new TaintRange(1,2));
        // new StringBuilder("FbAr").insert(1, "oO")    // FoObAr
        tb.insert(ts, srcTs, new StringBuilder("oO"), tgtTs, 1, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(4,5), untrusted(2,3)]", ts.toString());
    }

    @Test
    public void testInsertRange() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(2,4));
        // new StringBuilder("foar").insert(2, "zzOBzz", 2, 4)  // foOBar
        tb.insert(ts, srcTs, new StringBuilder("zzOBzz"), tgtTs, 2, 2, 4, 3);
        Assert.assertEquals("Taints:[untrusted(2,4)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0,1));
        tgtTs = new TaintRanges(new TaintRange(2,4));
        // new StringBuilder("Foar").insert(2, "zzOBzz", 2, 4)  // FoOBar
        tb.insert(ts, srcTs, new StringBuilder("zzOBzz"), tgtTs, 2, 2, 4, 3);
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(2,4)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3,4));
        tgtTs = new TaintRanges(new TaintRange(2,4));
        // new StringBuilder("foaR").insert(2, "zzOBzz", 2, 4)  // foOBaR
        tb.insert(ts, srcTs, new StringBuilder("zzOBzz"), tgtTs, 2, 2, 4, 3);
        Assert.assertEquals("Taints:[untrusted(5,6), untrusted(2,4)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(2,3));
        tgtTs = new TaintRanges(new TaintRange(2,4));
        // new StringBuilder("foAr").insert(2, "zzOBzz", 2, 4)  // foOBAr
        tb.insert(ts, srcTs, new StringBuilder("zzOBzz"), tgtTs, 2, 2, 4, 3);
        Assert.assertEquals("Taints:[untrusted(2,5)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1,3));
        tgtTs = new TaintRanges(new TaintRange(3,4));
        // new StringBuilder("fOAr").insert(2, "zzoBzz", 2, 4)  // fOoBAr
        tb.insert(ts, srcTs, new StringBuilder("zzoBzz"), tgtTs, 2, 2, 4, 3);
        Assert.assertEquals("Taints:[untrusted(1,2), untrusted(3,5)]", ts.toString());
    }
}
