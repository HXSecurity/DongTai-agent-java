package com.secnium.iast.core.util;

import org.junit.Test;

public class TaintPoolUtilsTest {
    @Test
    public void testSystemIdentify() {
        String dynamicString = new String("owef");
        String staticString = "owef";

        System.out.println(dynamicString == staticString);
        System.out.println(dynamicString.equals(staticString));
        System.out.println(dynamicString.hashCode() == staticString.hashCode());
        System.out.println(System.identityHashCode(dynamicString) == System.identityHashCode(staticString));
    }
}
