package com.secnium.iast.core.context;

import io.dongtai.iast.core.handler.context.TracingContext;
import org.junit.Test;

public class TracingContextTest {

    @Test
    public void testParseTraceId() {
        TracingContext context = new TracingContext();
        context.parseOrCreateTraceId("aslkdfj", 1);
        System.out.println(context);

        context.parseOrCreateTraceId("6a386bd4b1e84c83a3b4be891089fae4.1.1.1", 1);
        System.out.println(context);

        context.parseOrCreateTraceId("6a386bd4b1e84c83a3b4be891089fae4.1.1", 1);
        System.out.println(context);

        context.parseOrCreateTraceId(null, 1);
        System.out.println(context);
    }

    @Test
    public void testParseOrCreateTraceId() {
        TracingContext context = new TracingContext();
        context.parseOrCreateTraceId("aslkdfj", 1);
        System.out.println(context);

        String segmentId = context.createSegmentId();
        System.out.println(segmentId);
    }
}
