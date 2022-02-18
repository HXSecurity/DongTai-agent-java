package io.dongtai.iast.core.handler.context.ids;

import org.junit.Test;

public class GlobalIdGeneratorTest {

    @Test
    public void testGenerate() {
        String traceId = GlobalIdGenerator.generate(1);
        System.out.println(traceId);

        System.out.println(GlobalIdGenerator.generate(1));

        System.out.println(GlobalIdGenerator.generate(1));
    }

}
