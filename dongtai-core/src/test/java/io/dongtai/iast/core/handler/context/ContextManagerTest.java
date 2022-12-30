package io.dongtai.iast.core.handler.context;

import org.junit.Assert;
import org.junit.Test;

public class ContextManagerTest {
    @Test
    public void testTracingContext() {
        String traceId1 = ContextManager.currentTraceId();
        TracingContext context1 = ContextManager.getContext().get();
        Assert.assertEquals(context1.getLevel(), 0);
        Assert.assertEquals(context1.getParentId(), "0");

        ContextManager.parseTraceId(traceId1);
        TracingContext context2 = ContextManager.getContext().get();
        Assert.assertEquals(context1, context2);

        String traceId3 = ContextManager.nextTraceId();
        ContextManager.parseTraceId(traceId3);
        TracingContext context3 = ContextManager.getContext().get();
        Assert.assertNotEquals(context1, context3);
        Assert.assertEquals(context1.getGlobalId(), context3.getGlobalId());
        Assert.assertEquals(context1.getSpanId(), context3.getParentId());
        Assert.assertEquals(context3.getLevel(), 1);
        Assert.assertNotEquals(context1.getSpanId(), context3.getSpanId());

        ContextManager.currentTraceId();
        TracingContext context4 = ContextManager.getContext().get();
        Assert.assertEquals(context3, context4);

        ContextManager.getContext().remove();
        ContextManager.currentTraceId();
        TracingContext context5 = ContextManager.getContext().get();
        Assert.assertNotEquals(context4.getGlobalId(), context5.getGlobalId());

        ContextManager.getContext().remove();
    }
}
