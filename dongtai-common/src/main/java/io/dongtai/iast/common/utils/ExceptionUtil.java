package io.dongtai.iast.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 处理异常相关的公共逻辑提取到这里
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class ExceptionUtil {

    /**
     * 把printStackTrace会打印的内容以字符串的形式返回
     *
     * @param e
     * @return
     */
    public static String getPrintStackTraceString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

}
