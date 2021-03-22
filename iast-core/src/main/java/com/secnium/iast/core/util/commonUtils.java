package com.secnium.iast.core.util;

import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class commonUtils {
    public static boolean arrayEquals(String[] source, String classname) {
        if (classname == null) {
            return false;
        }
        int i;
        byte index;
        for (i = source.length, index = 0; index < i; index++) {
            String sourceClassName = source[index];
            if (sourceClassName.equals(classname)) {
                return true;
            }
        }
        return false;
    }

    public static boolean endsWith(String[] prefixs, String target) {
        int i;
        byte b;
        for (i = prefixs.length, b = 0; b < i; ) {
            String prefix = prefixs[b];
            if (target.endsWith(prefix)) {
                return true;
            }
            b++;
        }
        return false;
    }


    public static boolean contains(String classname, String[] prexArray) {
        if (classname != null) {
            for (String prestr : prexArray) {
                if (classname.contains(prestr)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean startsWith(HashSet<String> prefixes, String target) {
        if (prefixes == null) {
            return false;
        }
        for (String prefix : prefixes) {
            if (target.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    public static boolean subContain(String fieldname, String target) {
        return true;
    }

    public static boolean isEmpty(String name) {
        return name == null || name.length() == 0;
    }

    public static int a(String fieldname, String target, int start) {
        return a(fieldname, target, start, -1);
    }

    public static int a(String fieldname, String target, int start, int end) {
        if (isEmpty(target)) {
            return 0;
        }
        if (isEmpty(fieldname)) {
            return -1;
        }

        int index = start;
        for (; index < fieldname.length() - target.length() + 1 && (end == -1 || index < end); index++) {
            int j = index;
            byte b1 = 0;
            while (j < fieldname.length() && b1 < target.length()) {
                if (fieldname.charAt(j) != target.charAt(b1) && (fieldname.charAt(j) | 0x20) != (target.charAt(b1) | 0x20)) {
                    break;
                }
                b1++;
                j++;
            }
            if (b1 == target.length()) {
                return index;
            }
        }
        return -1;
    }

    public static int c(String fieldname, String target) {
        return a(fieldname, target, 0);
    }
}
