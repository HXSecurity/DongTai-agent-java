package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TaintRangeTest {
    @Test
    public void testOverlaps() {
        TaintRange tr1;
        TaintRange tr2;
        tr1 = new TaintRange(5, 10);

        Map<TaintRange, Boolean> tests = new HashMap<TaintRange, Boolean>() {{
            put(new TaintRange(1, 4), false);
            put(new TaintRange(1, 5), true);
            put(new TaintRange(1, 11), true);
            put(new TaintRange(9, 16), true);
            put(new TaintRange(10, 16), true);
            put(new TaintRange(11, 16), false);
        }};
        for (Map.Entry<TaintRange, Boolean> entry : tests.entrySet()) {
            tr2 = entry.getKey();
            Assert.assertEquals(tr1.toString() + " | " + tr2.toString(), entry.getValue(), tr1.overlaps(tr2));
        }
    }

    @Test
    public void testCompareRange() {
        TaintRange base;
        base = new TaintRange(5, 10);

        Map<TaintRange, TaintRange.RangeRelation> tests = new HashMap<TaintRange, TaintRange.RangeRelation>() {{
            put(new TaintRange(1, 4), TaintRange.RangeRelation.BELOW);
            put(new TaintRange(1, 5), TaintRange.RangeRelation.BELOW);
            put(new TaintRange(1, 6), TaintRange.RangeRelation.LOW_SPAN);
            put(new TaintRange(1, 10), TaintRange.RangeRelation.LOW_SPAN);
            put(new TaintRange(1, 11), TaintRange.RangeRelation.CONTAIN);
            put(new TaintRange(5, 10), TaintRange.RangeRelation.WITHIN);
            put(new TaintRange(5, 9), TaintRange.RangeRelation.WITHIN);
            put(new TaintRange(6, 10), TaintRange.RangeRelation.WITHIN);
            put(new TaintRange(6, 9), TaintRange.RangeRelation.WITHIN);
            put(new TaintRange(5, 11), TaintRange.RangeRelation.HIGH_SPAN);
            put(new TaintRange(6, 11), TaintRange.RangeRelation.HIGH_SPAN);
            put(new TaintRange(10, 12), TaintRange.RangeRelation.ABOVE);
            put(new TaintRange(11, 12), TaintRange.RangeRelation.ABOVE);
        }};

        for (Map.Entry<TaintRange, TaintRange.RangeRelation> entry : tests.entrySet()) {
            TaintRange tr = entry.getKey();
            Assert.assertEquals(base.toString() + " | " + tr.toString(), entry.getValue(), tr.compareRange(base.start, base.stop));
        }
    }
}
