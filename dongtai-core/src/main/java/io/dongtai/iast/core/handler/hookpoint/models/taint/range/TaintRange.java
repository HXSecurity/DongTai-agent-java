package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;

public class TaintRange {
    private String name;
    public int start;
    public int stop;

    public enum RangeRelation {
        BELOW,
        LOW_SPAN,
        WITHIN,
        CONTAIN,
        HIGH_SPAN,
        ABOVE
    }

    public TaintRange(int start, int stop) {
        this(TaintTag.UNTRUSTED.getKey(), start, stop);
    }

    public TaintRange(String name, int start, int stop) {
        if (stop <= start) {
            throw new RuntimeException("invalid taint range: " + name + ", stop: " + stop + " must greater than start: " + start);
        }
        this.name = name;
        this.start = start;
        this.stop = stop;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int start) {
        if (start < 0) {
            throw new RuntimeException("invalid taint range: " + name + ", start:" + start + " must greater than 0");
        }
        this.start = start;
    }

    public int getStop() {
        return this.stop;
    }

    public void setStop(int stop) {
        if (stop <= this.start) {
            throw new RuntimeException("invalid taint range: " + name + ", stop: " + stop + " must greater than start: " + this.start);
        }
        this.stop = stop;
    }

    public int width() {
        return this.stop - this.start;
    }

    public TaintRange clone() {
        try {
            return new TaintRange(this.name, this.start, this.stop);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public boolean overlaps(TaintRange range) {
        return range.name.equals(this.name) && this.start <= range.stop && range.start <= this.stop;
    }

    /**
     * merge when two taint range overlaps
     * @param range TaintRange
     */
    public void merge(TaintRange range) {
        if (range.start < this.start) {
            this.start = range.start;
        }
        if (range.stop > this.stop) {
            this.stop = range.stop;
        }
    }

    public RangeRelation compareRange(int low, int high) {
        if (high <= low) {
            throw new RuntimeException("invalid compare, high: " + high + " must greater than low: " + low);
        }

        if (this.start < low && this.stop <= low) {
            // |-----|
            // |------|
            //        |------|
            return RangeRelation.BELOW;
        } else if (this.start < low && this.stop <= high) {
            // |----------|
            // |-------------|
            //        |------|
            return RangeRelation.LOW_SPAN;
        } else if (this.start < low) {
            // |-------------------|
            //        |------|
            return RangeRelation.CONTAIN;
        } else if (this.start < high && this.stop <= high) {
            //         |----|
            //        |-----|
            //         |-----|
            //        |------|
            //        |------|
            return RangeRelation.WITHIN;
        } else if (this.start < high) {
            //       |------|
            // |----------|
            // |------|
            return RangeRelation.HIGH_SPAN;
        } else {
            //        |------|
            //         |------|
            // |------|
            return RangeRelation.ABOVE;
        }
    }

    public String toString() {
        return this.name + "(" + this.start + "," + this.stop + ")";
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("start", this.start);
        json.put("stop", this.stop);
        return json;
    }
}
