package com.secnium.iast.core;

import io.dongtai.log.DongTaiLog;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Locale;

public class JDKVersionIdentifyTest {
    @Test
    public void testJreCompat() {
        System.out.println("[+] test java runtime version identify");
        try {
            Method m = Locale.class.getMethod("forLanguageTag", String.class);
            System.out.println("m = " + m);
        } catch (NoSuchMethodException e) {
            DongTaiLog.error(e);
        }
    }
}
