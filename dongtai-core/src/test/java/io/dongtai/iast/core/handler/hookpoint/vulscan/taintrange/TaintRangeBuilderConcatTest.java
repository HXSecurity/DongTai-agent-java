package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaintRangeBuilderConcatTest {
    @Test
    public void testConcat() throws IOException {
        TaintRanges ts;
        TaintRanges srcTs;
        TaintRanges tgtTs;
        TaintRangesBuilder tb = new TaintRangesBuilder();

        ts = new TaintRanges();
        srcTs = new TaintRanges(new TaintRange(1, 3));
        tgtTs = new TaintRanges(new TaintRange(0, 2));
        // "fOO".concat("BAr")     // fOOBAr
        tb.concat(ts, "fOOBAr", srcTs, "BAr", tgtTs, new String[]{"BAr"});
        Assert.assertEquals("Taints:[untrusted(1,5)]", ts.toString());
    }
}
