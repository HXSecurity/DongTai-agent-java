package io.dongtai.iast.core.handler.context;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContextManagerTest {
    @Test
    public void testGetOrCreateGlobalTraceId() {
        String traceId = ContextManager.getOrCreateGlobalTraceId("", 123);
        System.out.println(traceId);

        String traceId2 = ContextManager.getOrCreateGlobalTraceId(traceId, 123);
        System.out.println(traceId2);
        assertEquals(traceId2, traceId);

        String traceId3 = ContextManager.getSegmentId();
        System.out.println(traceId3);
        assertNotEquals(traceId3, traceId);
    }
}
