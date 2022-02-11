package io.dongtai.iast.core.utils;

import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class commonUtils {


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


    public static boolean subContain(String fieldName, String item) {
        return fieldName.equalsIgnoreCase(item);
    }

    public static boolean isEmpty(String name) {
        return name == null || name.length() == 0;
    }

}
