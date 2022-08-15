package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderSubsetTest {
    @Test
    public void testSubsetStart() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,6));
        // new StringBuilder("foobar").substring(3)
        tb.subset(ts, srcTs, new StringBuilder("foobar"), tgtTs, 3, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,6));
        // new StringBuilder("foobar").substring(2)
        tb.subset(ts, srcTs, new StringBuilder("foobar"), tgtTs, 2, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(1,4)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,5));
        // new StringBuilder("foobar").substring(4)
        tb.subset(ts, srcTs, new StringBuilder("foobar"), tgtTs, 4, 0, 0, 1);
        Assert.assertEquals("Taints:[untrusted(0,1)]", ts.toString());
    }

    @Test
    public void testSubsetStartStop() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,6));
        // new StringBuilder("foobarbaz").substring(3, 6)
        tb.subset(ts, srcTs, new StringBuilder("foobarbaz"), tgtTs, 3, 6, 0, 2);
        Assert.assertEquals("Taints:[untrusted(0,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,6));
        // new StringBuilder("foobarbaz").substring(2, 5)
        tb.subset(ts, srcTs, new StringBuilder("foobarbaz"), tgtTs, 2, 5, 0, 2);
        Assert.assertEquals("Taints:[untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3,5));
        // new StringBuilder("foobarbaz").substring(4, 7)
        tb.subset(ts, srcTs, new StringBuilder("foobarbaz"), tgtTs, 4, 7, 0, 2);
        Assert.assertEquals("Taints:[untrusted(0,1)]", ts.toString());
    }

    @Test
    public void testSubsetGetChars() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(1,3));
        // new StringBuilder("foo").getChars(0, 3, chars, 0)
        tb.subset(ts, srcTs, new StringBuilder("foo"), tgtTs, 0, 3, 0, 3);
        Assert.assertEquals("Taints:[untrusted(1,3)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1,3));
        tgtTs = new TaintRanges(new TaintRange(2,5));
        // chars: "foo"
        // new StringBuilder("11bar22").getChars(2, 5, chars, 3)
        tb.subset(ts, srcTs, new StringBuilder("foo"), tgtTs, 2, 5, 3, 3);
        Assert.assertEquals("Taints:[untrusted(1,6)]", ts.toString());

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1,2));
        tgtTs = new TaintRanges(new TaintRange(2,5));
        // chars: "foo"
        // new StringBuilder("11bar22").getChars(2, 5, chars, 3)
        tb.subset(ts, srcTs, new StringBuilder("foo"), tgtTs, 2, 5, 3, 3);
        Assert.assertEquals("Taints:[untrusted(1,2), untrusted(3,6)]", ts.toString());
    }
}
