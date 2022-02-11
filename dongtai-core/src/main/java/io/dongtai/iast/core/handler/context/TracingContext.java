package io.dongtai.iast.core.handler.context;

import io.dongtai.iast.core.handler.context.ids.GlobalIdGenerator;
import lombok.Getter;
import lombok.Setter;

/**
 * @author owefsad
 */
public class TracingContext {

    @Setter
    @Getter
    private String traceId;

    @Setter
    @Getter
    private String applicationId;

    @Setter
    @Getter
    private int spanId;

    @Setter
    @Getter
    private int parentAgent;

    @Setter
    @Getter
    private int currentAgent;

    private int getNextSpanId() {
        return this.spanId + 1;
    }

    public String createSegmentId() {
        return getApplicationId() + "." + getCurrentAgent() + "." + Thread.currentThread().getId() + "."
                + getNextSpanId();
    }

    public static String getHeaderKey() {
        return "dt-traceid";
    }

    public void parseOrCreateTraceId(String traceId, int agentId) {
        this.setTraceId(traceId);
        String[] traceItem = traceId != null ? traceId.split("\\.") : null;
        if (traceItem == null || traceItem.length != 4) {
            String newTraceId = GlobalIdGenerator.generate(agentId);
            traceItem = newTraceId.split("\\.");
            this.setTraceId(newTraceId);
        }
        this.setApplicationId(traceItem[0]);
        this.setParentAgent(Integer.parseInt(traceItem[1]));
        this.setSpanId(Integer.parseInt(traceItem[3]));
        this.setCurrentAgent(agentId);
    }

    @Override
    public String toString() {
        return "TracingContext{" +
                "traceId='" + traceId + '\'' +
                ", spanId=" + spanId +
                ", parentAgent=" + parentAgent +
                ", currentAgent=" + currentAgent +
                '}';
    }
}
