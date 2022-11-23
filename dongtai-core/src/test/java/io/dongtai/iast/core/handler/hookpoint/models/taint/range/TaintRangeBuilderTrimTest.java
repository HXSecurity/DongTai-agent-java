package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaintRangeBuilderTrimTest {
    @Test
    public void testTrim() {
        TaintRanges ts;

        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 7));
        // "  fOOBAr ".trim()     // fOOBAr
        tb.trim(TaintCommand.TRIM, ts, "  fOOBAr ", srcTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }

    @Test
    public void testTrimLeft() {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(3, 7));
        // "  fOOBAr".trim()     // fOOBAr
        tb.trim(TaintCommand.TRIM_LEFT, ts, "  fOOBAr", srcTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }

    @Test
    public void testTrimRight() throws IOException {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1, 5));
        // "fOOBAr ".trim()     // fOOBAr
        tb.trim(TaintCommand.TRIM_RIGHT, ts, "fOOBAr ", srcTs, 0);
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }
}
