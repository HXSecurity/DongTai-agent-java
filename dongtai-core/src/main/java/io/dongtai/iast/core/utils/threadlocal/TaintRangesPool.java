package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;

import java.util.Map;

public class TaintRangesPool extends ThreadLocal<Map<Integer, TaintRanges>> {
    @Override
    protected Map<Integer, TaintRanges> initialValue() {
        return null;
    }

    public void add(Integer hash, TaintRanges taintRanges) {
        this.get().put(hash, taintRanges);
    }

    public TaintRanges get(int hash) {
        return this.get().get(hash);
    }
}
