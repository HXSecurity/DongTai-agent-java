package com.secnium.iast.core.util;

import org.junit.Test;

public class AssertsTest {
    @Test
    public void NOT_NULL() {
        Asserts.NOT_NULL("AssertsTest", null);
    }
}
