package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import java.util.*;

public class TaintRanges {
    private ArrayList<TaintRange> taintRanges;

    public TaintRanges() {
        this.taintRanges = new ArrayList<TaintRange>();
    }

    public TaintRanges(ArrayList<TaintRange> taintRanges) {
        this.taintRanges = taintRanges;
    }

    public TaintRanges(TaintRange ...taintRanges) {
        this.taintRanges = new ArrayList<TaintRange>(Arrays.asList(taintRanges));
    }

    public ArrayList<TaintRange> getTaintRanges() {
        return this.taintRanges;
    }

    public void add(TaintRange taintRange) {
        this.taintRanges.add(taintRange);
    }

    public void addAll(TaintRanges taintRanges) {
        if (taintRanges != null) {
            this.taintRanges.addAll(taintRanges.getTaintRanges());
        }
    }

    public TaintRanges clone() {
        TaintRanges taintRanges = new TaintRanges();
        int size = this.taintRanges.size();
        for (int i = 0; i < size; i++) {
            taintRanges.taintRanges.add(this.taintRanges.get(i).clone());
        }
        return taintRanges;
    }

    public void shift(int i) {
        for (TaintRange taintRange : this.taintRanges) {
            if (taintRange.start + i < 0 || taintRange.stop + i < 0) {
                throw new RuntimeException("taint range shift range into negative value: " + i);
            }
            taintRange.start += i;
            taintRange.stop += i;
        }
    }

    public void trim(int start, int end) {
        if (start < 0) {
            throw new RuntimeException("taint range trim invalid start: " + start);
        }
        if (end < start) {
            throw new RuntimeException("taint range trim invalid stop: " + end + " < start: " + start);
        }
        if (end == start) {
            this.taintRanges.clear();
            return;
        }

        Iterator<TaintRange> it = this.taintRanges.iterator();
        while (it.hasNext()) {
            TaintRange next = it.next();
            switch (next.compareRange(start, end)) {
                case BELOW:
                case ABOVE:
                    it.remove();
                    break;
                case LOW_SPAN:
                    next.start = 0;
                    next.stop -= start;
                    break;
                case WITHIN:
                    next.start -= start;
                    next.stop -= start;
                    break;
                case CONTAIN:
                    next.start = 0;
                    next.stop = end - start;
                    break;
                case HIGH_SPAN:
                    next.start -= start;
                    next.stop = end - start;
                    break;
                default:
                    break;
            }
        }
    }

    public void split(int start, int stop) {
        if (start < 0) {
            throw new RuntimeException("taint range split invalid start: " + start);
        }
        if (stop < start) {
            throw new RuntimeException("taint range split invalid stop: " + stop + " < start:" + start);
        }
        if (stop != start) {
            int width = stop - start;
            List<TaintRange> newTaintRange = new ArrayList<TaintRange>();
            for (TaintRange taintRange : this.taintRanges) {
                if (start <= taintRange.stop) {
                    if (start > taintRange.start && start < taintRange.stop) {
                        newTaintRange.add(new TaintRange(taintRange.getName(), stop, taintRange.stop + width));
                        taintRange.stop = start;
                    } else if (start <= taintRange.start) {
                        taintRange.start += width;
                        taintRange.stop += width;
                    }
                }
            }
            this.taintRanges.addAll(newTaintRange);
        }
    }

    public void subRange(int start, int stop) {
        if (start < 0) {
            throw new RuntimeException("taint range subRange invalid start: " + start);
        }
        if (stop < start) {
            throw new RuntimeException("taint range subRange invalid stop: " + stop + " < start:" + start);
        }
        if (stop == start) {
            this.taintRanges.clear();
            return;
        }
        Iterator<TaintRange> it = this.taintRanges.iterator();
        while (it.hasNext()) {
            TaintRange next = it.next();
            switch (next.compareRange(start, stop)) {
                case BELOW:
                case ABOVE:
                    it.remove();
                    break;
                default:
                    next.start = Math.max(next.start, start);
                    next.stop = Math.min(next.stop, stop);
                    break;
            }
        }
    }

    public void clear(int start, int stop) {
        if (start < 0) {
            throw new RuntimeException("taint range clear invalid start: " + start);
        }
        if (stop <= start) {
            throw new RuntimeException("taint range clear invalid stop: " + stop + " <= start:" + start);
        }
        Iterator<TaintRange> it = this.taintRanges.iterator();
        TaintRange taintRange = null;
        while (it.hasNext()) {
            TaintRange next2, next1 = it.next();
            switch (next1.compareRange(start, stop)) {
                case LOW_SPAN:
                    next1.stop = start;
                    break;
                case WITHIN:
                    it.remove();
                    break;
                case CONTAIN:
                    next2 = next1.clone();
                    next1.stop = start;
                    next2.start = stop;
                    taintRange = next2;
                    break;
                case HIGH_SPAN:
                    next1.start = stop;
                    break;
                default:
            }
        }
        if (taintRange != null) {
            add(taintRange);
        }
    }

    public void merge() {
        if (this.taintRanges.size() <= 1) {
            return;
        }

        for (int i = this.taintRanges.size() - 1; i >= 0; i--) {
            TaintRange range1 = this.taintRanges.get(i);
            for (int j = this.taintRanges.size() - 1; j >= 0; j--) {
                if (j == i) {
                    continue;
                }
                TaintRange range2 = this.taintRanges.get(j);
                if (range1.overlaps(range2)) {
                    range1.merge(range2);
                    this.taintRanges.remove(j);
                    if (i > this.taintRanges.size() - 1) {
                        i = this.taintRanges.size();
                    }
                }
            }
        }
    }

    public String toString() {
        return "Taints:" + this.taintRanges;
    }
}
