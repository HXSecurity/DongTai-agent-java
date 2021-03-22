package com.secnium.iast.core.util;

public class StringUtils {

    public static boolean match(String source, String target) {
        // 完全匹配
        return fullMatch(source, target);
        //return fullMatch(source, target) || containMatch(source, target);
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

    public static String[] ConvertProcessImplArgToString(Object[] args) {
        if (args[0] != null && args[1] != null) {
            return new String[]{
                    ConvertArrayToString((String[]) args[0]),
                    ConvertArrayToString((String[]) args[1])
            };
        } else if (args[0] != null) {
            return new String[]{
                    ConvertArrayToString((String[]) args[0])
            };
        } else if (args[1] != null) {
            return new String[]{
                    ConvertArrayToString((String[]) args[1])
            };
        } else {
            return null;
        }
    }

    public static String ConvertArrayToString(String[] strs) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str).append(" ");
        }
        return sb.toString();
    }

    private static boolean fullMatch(String source, String target) {
        return source == target;
    }
}
