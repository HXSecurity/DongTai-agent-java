package io.dongtai.iast.core.utils;

import java.util.Objects;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class StringUtils {

    public static boolean match(String source, String target) {
        // 完全匹配
        return fullMatch(source, target);
    }

    public static int[] convertStringToIntArray(String string) {
        if (string.startsWith("P")) {
            string = string.substring(1);
        }

        String[] positions = string.split(",");
        int[] intPositions = new int[positions.length];
        int index = 0;
        for (String pos : positions) {
            intPositions[index++] = Integer.parseInt(pos) - 1;
        }
        return intPositions;
    }

    private static boolean fullMatch(String source, String target) {
        return Objects.equals(source, target);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String normalize(String str, int maxLength) {
        int max = Math.max(maxLength, 5);
        if (str != null && str.length() != 0 && str.length() > max) {
            int middle = (max - 3) / 2;
            String sb = str.substring(0, (1 - (max % 2)) + middle) +
                    "..." +
                    str.substring(str.length() - middle);
            return sb;
        }
        return str;
    }
}
