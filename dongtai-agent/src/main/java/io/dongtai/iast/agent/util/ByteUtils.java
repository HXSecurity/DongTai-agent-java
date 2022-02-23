package io.dongtai.iast.agent.util;

/**
 * 字节转换工具
 *
 * @author ThinkGem
 * @version 2015-6-20
 */
public class ByteUtils {

    private static final int UNIT = 1024;

    /**
     * @param byteSize 字节
     * @return
     */
    public static String formatByteSize(long byteSize) {

        if (byteSize <= -1) {
            return String.valueOf(byteSize);
        }

        double size = 1.0 * byteSize;

        String type = "B";
        //不足1KB
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "B";
            return format(size, type);
        }

        size = size / UNIT;
        //不足1MB
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "KB";
            return format(size, type);
        }

        size = size / UNIT;
        //不足1GB
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "MB";
            return format(size, type);
        }

        size = size / UNIT;
        //不足1TB
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "GB";
            return format(size, type);
        }

        size = size / UNIT;
        //不足1PB
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "TB";
            return format(size, type);
        }

        size = size / UNIT;
        if ((int) Math.floor(size / UNIT) <= 0) {
            type = "PB";
            return format(size, type);
        }
        return ">PB";
    }

    private static String format(double size, String type) {
        int precision = 0;

        if (size * 1000 % 10 > 0) {
            precision = 3;
        } else if (size * 100 % 10 > 0) {
            precision = 2;
        } else if (size * 10 % 10 > 0) {
            precision = 1;
        }

        String formatStr = "%." + precision + "f";

        if ("KB".equals(type)) {
            return String.format(formatStr, (size)) + "KB";
        } else if ("MB".equals(type)) {
            return String.format(formatStr, (size)) + "MB";
        } else if ("GB".equals(type)) {
            return String.format(formatStr, (size)) + "GB";
        } else if ("TB".equals(type)) {
            return String.format(formatStr, (size)) + "TB";
        } else if ("PB".equals(type)) {
            return String.format(formatStr, (size)) + "PB";
        }
        return String.format(formatStr, (size)) + "B";
    }
}
