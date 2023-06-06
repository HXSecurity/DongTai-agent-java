package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;

import java.util.Map;

public class TaintRangesPool extends ThreadLocal<Map<Long, TaintRanges>> {
    @Override
    protected Map<Long, TaintRanges> initialValue() {
        return null;
    }

    public void add(Long hash, TaintRanges taintRanges) {
        this.get().put(hash, taintRanges);
    }

    public TaintRanges get(long hash) {
        return this.get().get(hash);
    }
}
