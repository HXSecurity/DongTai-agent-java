package io.dongtai.iast.core.handler.context.ids;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class IdGeneratorTest {
    @Test
    public void testNewGlobalId() {
        Assert.assertEquals("newGlobalId length", IdGenerator.newGlobalId().length(), 32);

        List<String> ids = new LinkedList<String>();
        for (int i = 0; i < 1000; i++) {
            String gId = IdGenerator.newGlobalId();
            Assert.assertFalse("newGlobalId", ids.contains(gId));
            ids.add(gId);
        }
        ids.clear();
    }

    @Test
    public void testNewSpanId() {
        Assert.assertEquals("newSpanId length", IdGenerator.newSpanId().length(), 16);

        List<String> ids = new LinkedList<String>();
        for (int i = 0; i < 1000; i++) {
            String spanId = IdGenerator.newSpanId();
            Assert.assertFalse("newSpanId", ids.contains(spanId));
            ids.add(spanId);
        }
        ids.clear();
    }
}
