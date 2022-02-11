package com.secnium.iast.core.util;

import io.dongtai.iast.core.utils.Asserts;
import org.junit.Test;

public class AssertTest {
    @Test
    public void NOT_NULL() {
        // 这个可以不用做单元测试
        Asserts.NOT_NULL("AssertTest", "PASS");
    }
}
