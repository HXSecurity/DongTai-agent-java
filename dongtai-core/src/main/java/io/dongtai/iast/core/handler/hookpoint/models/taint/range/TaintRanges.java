package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import com.alibaba.fastjson2.JSONArray;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.common.string.StringUtils;

import java.util.*;

public class TaintRanges {
    private final ArrayList<TaintRange> taintRanges;

    public TaintRanges() {
        this.taintRanges = new ArrayList<TaintRange>();
    }

    public TaintRanges(ArrayList<TaintRange> taintRanges) {
        this.taintRanges = taintRanges;
    }

    public TaintRanges(TaintRange... taintRanges) {
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

    public void addAll(Collection<TaintRange> taintRanges) {
        if (taintRanges != null) {
            this.taintRanges.addAll(taintRanges);
        }
    }

    public void untag(String[] untags) {
        if (untags == null || untags.length == 0 || this.taintRanges.size() == 0) {
            return;
        }
        for (String str : untags) {
            if (StringUtils.isEmpty(str)) {
                continue;
            }
            for (Iterator<TaintRange> it = this.taintRanges.listIterator(); it.hasNext(); ) {
                if (it.next().getName().equals(str)) {
                    it.remove();
                }
            }
        }
    }

    public boolean hasRequiredTaintTags(TaintTag[] tags) {
        if (tags == null) {
            return true;
        }
        int total = tags.length;
        Map<String, Boolean> found = new HashMap<String, Boolean>();
        for (TaintTag tag : tags) {
            for (TaintRange taintRange : this.taintRanges) {
                if (tag.equals(taintRange.getName())) {
                    found.put(tag.getKey(), true);
                }
            }
        }
        return total == found.size();
    }

    public boolean hasDisallowedTaintTags(TaintTag[] tags) {
        if (tags == null) {
            return false;
        }
        for (TaintTag tag : tags) {
            for (TaintRange taintRange : this.taintRanges) {
                if (tag.equals(taintRange.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasValidatedTags(TaintTag[] tags) {
        if (tags == null) {
            return false;
        }
        for (TaintTag tag : tags) {
            if (tag.equals(TaintTag.VALIDATED.getKey())) {
                return true;
            }
        }
        return false;
    }

    public TaintRanges clone() {
        TaintRanges taintRanges = new TaintRanges();
        int size = this.taintRanges.size();
        for (int i = 0; i < size; i++) {
            taintRanges.taintRanges.add(this.taintRanges.get(i).clone());
        }
        return taintRanges;
    }

    public boolean isEmpty() {
        return this.taintRanges == null || this.taintRanges.isEmpty();
    }

    public TaintRanges explode(int i) {
        if (i < 0) {
            throw new RuntimeException("taint range explode to a negative value: " + i);
        }
        for (TaintRange taintRange : this.taintRanges) {
            taintRange.start = 0;
            taintRange.stop = i;
        }
        return this;
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

    public void remove(int start, int stop) {
        if (start < 0) {
            throw new RuntimeException("taint range remove invalid start: " + start);
        }
        if (stop < start) {
            throw new RuntimeException("taint range remove invalid stop: " + stop + " < start:" + start);
        }
        if (stop != start) {
            int length = stop - start;
            Iterator<TaintRange> it = this.taintRanges.iterator();
            while (it.hasNext()) {
                TaintRange next = it.next();
                switch (next.compareRange(start, stop)) {
                    case LOW_SPAN:
                        next.stop = start;
                        break;
                    case WITHIN:
                        it.remove();
                        break;
                    case CONTAIN:
                        next.stop -= length;
                        break;
                    case HIGH_SPAN:
                        next.start = start;
                        next.stop -= stop - next.start;
                        break;
                    case ABOVE:
                        next.start -= length;
                        next.stop -= length;
                        break;
                }
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

    public JSONArray toJson() {
        JSONArray json = new JSONArray();
        for (TaintRange tr : this.taintRanges) {
            json.add(tr.toJson());
        }
        return json;
    }
}
