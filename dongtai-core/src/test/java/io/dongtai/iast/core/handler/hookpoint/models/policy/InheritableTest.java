package io.dongtai.iast.core.handler.hookpoint.models.policy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.*;

public class InheritableTest {
    @Test
    public void testParse() throws PolicyException {
        Map<String, Inheritable> tests = new HashMap<String, Inheritable>() {{
            put("false", Inheritable.SELF);
            put("true", Inheritable.SUBCLASS);
            put("all", Inheritable.ALL);
            put("All", Inheritable.ALL);
            put("ALL", Inheritable.ALL);
        }};

        for (Map.Entry<String, Inheritable> entry : tests.entrySet()) {
            Inheritable inheritable = Inheritable.parse(entry.getKey());
            Assert.assertEquals("parse " + entry.getKey(), entry.getValue(), inheritable);
        }

        List<String> exceptionTests = Arrays.asList(null, "", " false", "false ", "invalid");

        for (String s : exceptionTests) {
            Assert.assertThrows("parse exception " + s, PolicyException.class, new ThrowingRunnable() {
                @Override
                public void run() throws PolicyException {
                    Inheritable.parse(s);
                }
            });
        }
    }
}