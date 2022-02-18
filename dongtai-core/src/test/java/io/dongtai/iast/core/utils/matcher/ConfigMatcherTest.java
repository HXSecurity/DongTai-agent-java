package io.dongtai.iast.core.utils.matcher;

import org.junit.Test;

import java.util.regex.Pattern;

public class ConfigMatcherTest {
    /**
     * // CGLIB$$的类不hook
     * if (hook && className.contains("CGLIB$$")) {
     * hook = false;
     * DongTaiLog.debug("ignore transform {} in loader={}. Reason: classname is a aop class by CGLIB", className, loader);
     * }
     * <p>
     * //$$Lambda$
     * if (hook && className.contains("$$Lambda$")) {
     * hook = false;
     * DongTaiLog.debug("ignore transform {} in loader={}. Reason: classname is a aop class by Lambda", className, loader);
     * }
     * <p>
     * //$$Lambda$
     * if (hook && className.contains("_$$_jvst")) {
     * hook = false;
     * DongTaiLog.debug("ignore transform {} in loader={}. Reason: classname is a aop class", className, loader);
     * }
     * <p>
     * 执行时长：46329000 纳秒.
     */
    @Test
    public void testContainAndRe() {
        String className1 = "asdlkfjCGLIB$$asdlkfj";
        String className2 = "CGLIB$$asdlkfj";
        String className3 = "asdlkfjCGLIB$$";
        String keyword = "CGLIB$$";
        long stime = System.nanoTime();
        // 执行时间（1s）
        for (Integer i = 0; i < 1000000; i++) {
            className1.contains(keyword);
        }
        // 结束时间
        long etime = System.nanoTime();
        // 计算执行时间
        System.out.printf("执行时长：%d 纳秒.", (etime - stime));
    }

    /**
     * 2986873000
     */
    @Test
    public void testContainWithRe() {
        String className1 = "asdlkfjCGLIB$$asdlkfj";
        String className2 = "CGLIB$$asdlkfj";
        String className3 = "asdlkfjCGLIB$$";
        Pattern p = Pattern.compile(".*?CGLIB.*?");
        long stime = System.nanoTime();
        // 执行时间（1s）
        for (Integer i = 0; i < 1000000; i++) {
            p.matcher(className1).find();
        }
        // 结束时间
        long etime = System.nanoTime();
        // 计算执行时间
        System.out.printf("执行时长：%d 纳秒.", (etime - stime));
    }

    @Test
    public void testStartWithAndRe() {

    }
}
