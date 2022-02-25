package io.dongtai.iast.core.utils;

import io.dongtai.log.DongTaiLog;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ThrowableUtils {
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        DongTaiLog.error(sw.toString());
        return sw.toString();
    }
}
