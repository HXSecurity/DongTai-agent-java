package io.dongtai.iast.core.handler.hookpoint.models.policy;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PolicyNodeTypeTest {
    @Test
    public void testGet() {
        Map<Integer, PolicyNodeType> tests = new HashMap<Integer, PolicyNodeType>(){{
            put(null, null);
            put(0, null);
            put(1, PolicyNodeType.PROPAGATOR);
            put(2, PolicyNodeType.SOURCE);
            put(3, PolicyNodeType.VALIDATOR);
            put(4, PolicyNodeType.SINK);
            put(5, null);
        }};

        for (Map.Entry<Integer, PolicyNodeType> entry : tests.entrySet()) {
            Assert.assertEquals("get " + entry.getKey(), entry.getValue(), PolicyNodeType.get(entry.getKey()));
        }
    }
}