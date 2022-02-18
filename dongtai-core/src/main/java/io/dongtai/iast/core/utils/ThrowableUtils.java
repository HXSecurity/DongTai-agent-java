package io.dongtai.iast.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ThrowableUtils {
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
