package com.secnium.iast.core.util;

import org.junit.Test;

public class AssertsTest {
    @Test
    public void NOT_NULL() {
        // 这个可以不用做单元测试
        Asserts.NOT_NULL("AssertsTest", "PASS");
    }
}
