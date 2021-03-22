package com.secnium.iast.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ThrowableUtils {
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
