package io.dongtai.iast.common.string;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {


    @Test
    public void matchTest() {
        String raw = "foo";
        String compareValues = "foo";
        //完全相同比较
        Assert.assertTrue(StringUtils.match(raw, compareValues));

        String objectValue = new String("foo");
        //对象字符串与常量字符串比较，不完全相同
        Assert.assertFalse(StringUtils.match(raw, objectValue));
    }

    @Test
    public void testConvertStringToIntArray() {
        // 准备测试数据
        String input = "P1,2,3,4";
        // 调用被测试方法
        int[] result = StringUtils.convertStringToIntArray(input);
        // 期望的结果
        int[] expected = {0, 1, 2, 3}; // 输入中的每个数字减去1
        // 使用断言验证结果
        Assert.assertArrayEquals(expected, result);

        // 测试包含负数的情况
        input = "P-1,-2,-3,-4";

        result = StringUtils.convertStringToIntArray(input);

        expected = new int[]{-2, -3, -4, -5};

        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void isEmptyTest() {
        //判断空字符串
        String input = "";
        Assert.assertTrue(StringUtils.isEmpty(input));
        //判断空对象
        input = null;
        Assert.assertTrue(StringUtils.isEmpty(input));
        //判断有值字符串
        input = "in";
        Assert.assertFalse(StringUtils.isEmpty(input));
        //判断空格
        input = " ";
        Assert.assertFalse(StringUtils.isEmpty(input));


    }

    @Test
    public void isBlankTest(){
        //判断空字符串
        String input = "";
        Assert.assertTrue(StringUtils.isBlank(input));
        //判断空对象
        input = null;
        Assert.assertTrue(StringUtils.isBlank(input));
        //判断有值字符串
        input = "in";
        Assert.assertFalse(StringUtils.isBlank(input));
        //判断空格
        input = " ";
        Assert.assertTrue(StringUtils.isBlank(input));
    }

    @Test
    public void normalize() {
        int maxLength = 1024;
        String str;
        String nStr;
        str = new String(new char[1000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length", 1000, nStr.length());
        str = new String(new char[10000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length overflow", maxLength, nStr.length());

        maxLength = 7;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 7", "aa...aa", nStr);
        maxLength = 6;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 6", "aa...a", nStr);
    }

    @Test
    public void formatClassNameToDotDelimiter() {
        String s = StringUtils.formatClassNameToDotDelimiter("com/foo/bar");
        Assert.assertEquals("com.foo.bar", s);
    }

    @Test
    public void formatClassNameToSlashDelimiter() {
        String s = StringUtils.formatClassNameToSlashDelimiter("com.foo.bar");
        Assert.assertEquals("com/foo/bar", s);
    }

}