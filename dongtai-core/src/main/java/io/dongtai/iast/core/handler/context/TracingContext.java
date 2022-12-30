package io.dongtai.iast.core.handler.context;

import io.dongtai.iast.core.handler.context.ids.IdGenerator;

/**
 * @author owefsad
 */
public class TracingContext {
    private String globalId;

    private int level;

    private String parentId;

    private String spanId;
    private String cachedTraceId;

    public TracingContext() {
        this.globalId = IdGenerator.newGlobalId();
        this.level = 0;
        this.parentId = "0";
        this.spanId = IdGenerator.newSpanId();
    }

    public TracingContext(String globalId, int level, String parentId, String spanId) {
        this.globalId = globalId;
        this.level = level;
        this.parentId = parentId;
        this.spanId = spanId;
    }

    public String getGlobalId() {
        return this.globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSpanId() {
        return this.spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public static TracingContext getIncoming(String traceId) {
        String[] traceItem = traceId != null ? traceId.split("\\.") : null;
        if (traceItem == null || traceItem.length != 4) {
            return new TracingContext();
        }

        try {
            return new TracingContext(traceItem[0], Integer.parseInt(traceItem[1]), traceItem[2], traceItem[3]);
        } catch (NumberFormatException ignore) {
            return new TracingContext();
        }
    }

    public String newOutgoing() {
        int lvl = this.level + 1;
        String pid = this.spanId;
        String sid = IdGenerator.newSpanId();
        return this.globalId + "." + lvl + "." + pid + "." + sid;
    }

    @Override
    public String toString() {
        if (this.cachedTraceId != null && !this.cachedTraceId.isEmpty()) {
            return this.cachedTraceId;
        }
        return this.cachedTraceId = this.globalId + "." + this.level + "." + this.parentId + "." + this.spanId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() == obj.getClass()) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
