package io.dongtai.iast.common.string;

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

    /**
     * 判断字符串是否为空白字符串，比如 "  " 会被认为是空白字符串
     *
     * @param s
     * @return
     * @since 1.13.2
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
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

    /**
     * 类名字格式化为.分隔的形式
     * <p>
     * 比String直接replace快大约180倍左右
     *
     * @param className 要格式化的类名，比如 com/foo/bar
     * @return 替换后的类名，比如 com.foo.bar
     */
    public static String formatClassNameToDotDelimiter(String className) {
        return replaceChar(className, '/', '.');
    }

    /**
     * 类名字格式化为 / 分隔的形式
     *
     * @param className 要格式化的类名，比如 com.foo.bar
     * @return 替换后的类名，比如 com/foo/bar
     */
    public static String formatClassNameToSlashDelimiter(String className) {
        return replaceChar(className, '.', '/');
    }

    /**
     * 对字符串进行char级别的替换
     *
     * @param s        要转换的字符串
     * @param fromChar 把哪个字符
     * @param toChar   替换为目标字符
     * @return 替换后的字符串
     */
    public static String replaceChar(String s, char fromChar, char toChar) {
        if (s == null) {
            return null;
        }
        // 使用定长类型进行转换替换掉更复杂的replace，尽可能减少操作时间
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == fromChar) {
                chars[i] = toChar;
            }
        }
        return new String(chars);
    }

}
