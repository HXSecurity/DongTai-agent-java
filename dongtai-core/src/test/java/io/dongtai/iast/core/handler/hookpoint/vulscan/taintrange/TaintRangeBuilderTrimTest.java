package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaintRangeBuilderTrimTest {
    @Test
    public void testTrim() {
        TaintRanges ts;

        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3, 7));
        // "  fOOBAr ".trim()     // fOOBAr
        tb.trim(TaintRangesBuilder.Command.TRIM, ts, "  fOOBAr ", tgtTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }

    @Test
    public void testTrimLeft() {
        TaintRanges ts;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(3, 7));
        // "  fOOBAr".trim()     // fOOBAr
        tb.trim(TaintRangesBuilder.Command.TRIM_LEFT, ts, "  fOOBAr", tgtTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }

    @Test
    public void testTrimRight() throws IOException {
        TaintRanges ts;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        tgtTs = new TaintRanges(new TaintRange(1, 5));
        // "fOOBAr ".trim()     // fOOBAr
        tb.trim(TaintRangesBuilder.Command.TRIM_RIGHT, ts, "fOOBAr ", tgtTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }
}
