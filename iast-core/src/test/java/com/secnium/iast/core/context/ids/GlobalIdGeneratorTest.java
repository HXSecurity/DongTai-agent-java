package com.secnium.iast.core.context.ids;

import org.junit.Test;

public class GlobalIdGeneratorTest {

    @Test
    public void testGenerate() {
        String traceId = GlobalIdGenerator.generate(1);
        System.out.println(traceId);
    }

}
