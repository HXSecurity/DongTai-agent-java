package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRanges;

import java.util.HashMap;
import java.util.Map;

public class TaintRangesPool extends ThreadLocal<Map<Integer, TaintRanges>> {
    @Override
    protected Map<Integer, TaintRanges> initialValue() {
        return new HashMap<Integer, TaintRanges>();
    }

    public void add(Integer hash, TaintRanges taintRanges) {
        this.get().put(hash, taintRanges);
    }

    public TaintRanges get(int hash) {
        return this.get().get(hash);
    }
}
