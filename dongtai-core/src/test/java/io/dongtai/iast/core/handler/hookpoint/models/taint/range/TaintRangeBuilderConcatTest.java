package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaintRangeBuilderConcatTest {
    @Test
    public void testConcat() throws IOException {
        TaintRanges ts;
        TaintRanges oldTs;
        TaintRanges srcTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        oldTs = new TaintRanges(new TaintRange(1, 3));
        srcTs = new TaintRanges(new TaintRange(0, 2));
        // "fOO".concat("BAr")     // fOOBAr
        tb.concat(ts, "fOOBAr", oldTs, "BAr", srcTs, new String[]{"BAr"});
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }
}
