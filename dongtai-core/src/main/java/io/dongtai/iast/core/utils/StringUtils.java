package io.dongtai.iast.core.utils;

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
        return source == target;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String normalize(String str, int maxLength) {
        int max = Math.max(maxLength, 5);
        if (str != null && str.length() != 0 && str.length() > max) {
            int middle = (max - 3) / 2;
            StringBuilder sb = new StringBuilder();
            sb.append(str, 0, (1 - (max % 2)) + middle);
            sb.append("...");
            sb.append(str, str.length() - middle, str.length());
            return sb.toString();
        }
        return str;
    }
}
