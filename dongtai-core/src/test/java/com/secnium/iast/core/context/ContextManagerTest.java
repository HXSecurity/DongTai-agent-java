package com.secnium.iast.core.context;

import io.dongtai.iast.core.handler.context.ContextManager;
import org.junit.Test;

public class ContextManagerTest {

    @Test
    public void testGetGlobalTraceId() {
        String traceId = ContextManager.getOrCreateGlobalTraceId("", 1);
        System.out.println("Current TraceId: " + traceId);
        System.out.println("Next TraceId: " + ContextManager.getSegmentId());
        System.out.println("Next TraceId: " + ContextManager.getSegmentId());
    }
}
