package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

public class TaintRangeBuilderAppendTest {
    @Test
    public void testAppend() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 3));
        // "" + "FOO"
        tb.append(ts, new StringBuilder("FOO"), oldTs, "FOO", srcTs, 0, 0, 0);
        Assert.assertEquals("Taints:[untrusted(0,3)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 3));
        // "foo" + "bar"
        tb.append(ts, new StringBuilder("fooBAR"), oldTs, "BAR", srcTs, 0, 0, 0);
        Assert.assertEquals("Taints:[untrusted(3,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(0, 1));
        srcTs = new TaintRanges(new TaintRange(1, 2));
        // "Foo" + "bAr"
        tb.append(ts, new StringBuilder("FoobAr"), oldTs, "bAr", srcTs, 0, 0, 0);
        Assert.assertEquals("Taints:[untrusted(0,1), untrusted(4,5)]", ts.toString());
    }

    @Test
    public void testAppendStartStop() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 5));
        // StringBuilder("foo").append("ZBARZ", 1, 4)
        tb.append(ts, new StringBuilder("fooBAR"), oldTs, "ZBARZ", srcTs, 1, 4, 2);
        Assert.assertEquals("Taints:[untrusted(3,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 3));
        // StringBuilder("foo").append("ZBArz", 1, 4)
        tb.append(ts, new StringBuilder("fooBAr"), oldTs, "ZBArz", srcTs, 1, 4, 2);
        Assert.assertEquals("Taints:[untrusted(3,5)]", ts.toString());
    }

    @Test
    public void testAppendStartLength() {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 5));
        // StringBuilder("foo").append(char[]{"ZBARZ"}, 1, 3)
        tb.append(ts, new StringBuilder("fooBAR"), oldTs, "ZBARZ", srcTs, 1, 3, 3);
        Assert.assertEquals("Taints:[untrusted(3,6)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(0, 2));
        // StringBuilder("foo").append(char[]{"ZBarz"}, 1, 3)
        tb.append(ts, new StringBuilder("fooBar"), oldTs, "ZBarz", srcTs, 1, 3, 3);
        Assert.assertEquals("Taints:[untrusted(3,4)]", ts.toString());

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(2, 3));
        srcTs = new TaintRanges(new TaintRange(0, 2));
        // StringBuilder("foO").append(char[]{"ZBarz"}, 1, 3)
        tb.append(ts, new StringBuilder("foOBar"), oldTs, "ZBarz", srcTs, 1, 3, 3);
        Assert.assertEquals("Taints:[untrusted(2,4)]", ts.toString());
    }
}
