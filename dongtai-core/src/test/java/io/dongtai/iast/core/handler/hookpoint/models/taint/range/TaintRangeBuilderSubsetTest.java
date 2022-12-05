package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderSubsetTest {
    @Test
    public void testSubsetStart() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 6));
        // new StringBuilder("fooBAR").substring(3) // BAR
        tb.subset(ts, oldTs, new StringBuilder("fooBAR"), srcTs, 3, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,3)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 6));
        // new StringBuilder("fooBAR").substring(2) // oBAR
        tb.subset(ts, oldTs, new StringBuilder("fooBAR"), srcTs, 2, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(1,4)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 5));
        // new StringBuilder("fooBAr").substring(4)  // Ar
        tb.subset(ts, oldTs, new StringBuilder("fooBAr"), srcTs, 4, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,1)]", ts.toString());
    }

    @Test
    public void testSubsetStartStop() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 6));
        // new StringBuilder("fooBARbaz").substring(3, 6)   // BAR
        tb.subset(ts, oldTs, new StringBuilder("foobarbaz"), srcTs, 3, 6, 0, 2);
        Assert.assertEquals("Taints:[untrusted(0,3)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 6));
        // new StringBuilder("fooBARbaz").substring(2, 5)   // oBA
        tb.subset(ts, oldTs, new StringBuilder("foobarbaz"), srcTs, 2, 5, 0, 2);
        Assert.assertEquals("Taints:[untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 5));
        // new StringBuilder("fooBArbaz").substring(4, 7)   // Arb
        tb.subset(ts, oldTs, new StringBuilder("foobarbaz"), srcTs, 4, 7, 0, 2);
        Assert.assertEquals("Taints:[untrusted(0,1)]", ts.toString());
    }

    @Test
    public void testSubsetGetChars() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1, 3));
        // new StringBuilder("fOO").getChars(0, 3, chars, 0)    // fOO
        tb.subset(ts, oldTs, new StringBuilder("foo"), srcTs, 0, 3, 0, 3);
        Assert.assertEquals("Taints:[untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(1, 3));
        srcTs = new TaintRanges(new TaintRange(2, 5));
        // chars: "fOO"
        // new StringBuilder("zzBARzz").getChars(2, 5, chars, 3)    // fOOBAR
        tb.subset(ts, oldTs, new StringBuilder("foo"), srcTs, 2, 5, 3, 3);
        Assert.assertEquals("Taints:[untrusted(1,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(1, 2));
        srcTs = new TaintRanges(new TaintRange(2, 5));
        // chars: "fOo"
        // new StringBuilder("zzBARzz").getChars(2, 5, chars, 3)    // fOoBAR
        tb.subset(ts, oldTs, new StringBuilder("foo"), srcTs, 2, 5, 3, 3);
        Assert.assertEquals("Taints:[untrusted(1,2), untrusted(3,6)]", ts.toString());
    }
}
